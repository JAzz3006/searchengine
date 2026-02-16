package searchengine.services.search;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.SearchConfig;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.dto.response.SearchResultItem;
import searchengine.model.*;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageLemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.lemma.LemmaService;
import searchengine.util.html.HtmlParserUtils;
import searchengine.util.html.HtmlTextExtractor;
import searchengine.util.url.UrlNormalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {
    private static final Logger log = LoggerFactory.getLogger(RequestService.class);
    private final LemmaService lemmaService;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final PageLemmaRepository pageLemmaRepository;
    private final SitesList sitesList;
    private final SnippetBuilder snippetBuilder;

    //TODO: посмотреть в итоге какие методы оставить public, а какие перевести private

    public List<SearchResultItem> startSearch(String req, int offset, int limit, String siteUrl){
            List<SearchResultItem> resultItems = new ArrayList<>();
            if (siteUrl == null || siteUrl.isBlank()){
                for (SiteConfig siteConfig : sitesList.getSites()){
                    try{
                        resultItems.addAll(searchIfSiteExists(siteConfig.getUrl(), req));
                    }catch (Exception e){
                        log.warn("Сайт {} пропущен: {}", siteConfig.getUrl(), e.getMessage());
                    }
                }
            }else{
                resultItems.addAll(searchIfSiteExists(siteUrl,req));
            }
            return resultItems;
    }

    private List<SearchResultItem> searchIfSiteExists(String siteUrl, String req){
        String normalizedUrl = UrlNormalizer.normalizeSiteUrl(siteUrl);
        Site site = siteRepository.findByUrl(normalizedUrl)
                .orElseThrow(() -> new IllegalArgumentException("Сайт не найден в индексе"));
        if (site.getStatus()!= Status.INDEXED){
            throw new IllegalStateException("Сайт не проиндексирован");
        }
        return searchSite(site, req);
    }

    public List<SearchResultItem> searchSite(Site site, String req){

        List<SearchResultItem> results = new ArrayList<>();

        int pagesCount = pageRepository.countBySite(site);
        if (pagesCount == 0) return List.of();

        Set<String> requestLemmas = requestToLemmas(req);
        if (requestLemmas.isEmpty()) return List.of();

        List<Lemma> lemmasOfRequest = lemmaRepository
                .findAllByLemmaInAndSite(requestLemmas, site);

        excludedLemmasVoice(lemmasOfRequest, requestLemmas, site);

        Iterator<Lemma> lemmaIterator = lemmasOfRequest.iterator();
        while (lemmaIterator.hasNext()){
            Lemma l = lemmaIterator.next();
            float ratio = (float) l.getFrequency() / pagesCount;
            if (ratio > (SearchConfig.MAX_ALLOWED_LEMMA_FREQUENCY_RATIO)){
                log.warn("Лемма {} исключена из поиска из-за превышения маскимальной частоты", l.getLemma() );
                lemmaIterator.remove();
            }
        }

        if (lemmasOfRequest.isEmpty()) return List.of();

        lemmasOfRequest.sort(Comparator.comparingInt(Lemma::getFrequency));

        Set<Page> resultPagesSet = getResultPagesSet(lemmasOfRequest);

        Map<Long, Float>  pageAbsRelevance = calcPageAbsRelevance(resultPagesSet, lemmasOfRequest);

        for (Page page : resultPagesSet){
            SearchResultItem item = new SearchResultItem();
            item.setSite(site.getUrl());
            item.setSiteName(site.getName());
            item.setUri(page.getPath());
            item.setRelevance(pageAbsRelevance.get(page.getId()));
            item.setSnippet(snippetBuilder.buildSnippet(page, lemmasOfRequest));
            item.setTitle(HtmlParserUtils.extractTitle(page.getContent()));
        }

        return results;
    }

    private Map<Long, Float> calcPageAbsRelevance(Set<Page> resultPagesSet, List<Lemma> lemmasOfRequest) {
        Map<Long, Float> abs = new HashMap<>();
        Set<Long> pageIds = resultPagesSet.stream()
                .map(Page::getId)
                .collect(Collectors.toSet());
        Set<Long> lemmasIds = lemmasOfRequest.stream()
                .map(Lemma::getId)
                .collect(Collectors.toSet());

        List<PageLemma> pairs = pageLemmaRepository.findAllByPage_IdInAndLemma_IdIn(pageIds, lemmasIds);

        float maxRAbs = 0f;
        for (PageLemma pair : pairs){
            Long pageId = pair.getPage().getId();
            float newValue = abs.getOrDefault(pageId, 0f) + pair.getRank();
            abs.put(pageId, newValue);

            if (newValue > maxRAbs){
                maxRAbs = newValue;
            }
        }
        if (pairs.isEmpty()) return Map.of();

        for (Map.Entry<Long, Float> entry : abs.entrySet()){
            entry.setValue(entry.getValue() / maxRAbs);
        }
        return abs;
    }

    private Set<Page> getResultPagesSet(List<Lemma> lemmasOfRequest){
        if (lemmasOfRequest.isEmpty()) return Set.of();
        Lemma rarestLemma = lemmasOfRequest.get(0);
        Set<Page> pagesOfRarestLemma = new HashSet<>(pageLemmaRepository.findPagesByLemmaId(rarestLemma.getId()));
        for (int i = 1; i < lemmasOfRequest.size(); i++){
            Lemma currentLemma = lemmasOfRequest.get(i);
            Set<Page>currentPages = pageLemmaRepository.findPagesByLemmaId(currentLemma.getId());
            pagesOfRarestLemma.retainAll(currentPages);
            if (pagesOfRarestLemma.isEmpty()){
                return Set.of();
            }
        }
        return pagesOfRarestLemma;
    }

    private void excludedLemmasVoice(List<Lemma> listOfLemmas, Set<String> listOfStrings, Site site){
        Set<String> stringLemmas = listOfLemmas
                .stream()
                .map(Lemma::getLemma)
                .collect(Collectors.toSet());
        listOfStrings.stream()
                .filter(s -> !stringLemmas.contains(s))
                .forEach(s -> log.info(
                        "Лемма {} отсутствует в индексе сайта {} и не включена в посиковый запрос",
                        s,
                        site.getUrl()
                ));
    }

    public Set<String> requestToLemmas(String req){
        if (req == null || req.isBlank()){
            return Collections.emptySet();
        }
        return lemmaService.collectLemmas(req).keySet();
    }



}