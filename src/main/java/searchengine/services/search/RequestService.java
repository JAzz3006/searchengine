package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SearchConfig;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingService;
import searchengine.services.lemma.LemmaService;
import searchengine.services.statistics.StatisticsService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final LemmaService lemmaService;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final SitesList sitesList;

    public void startSearch(){
        //TODO: проверку на наличие сайта в БД и на наличие статуса INDEXED
        //посмотреть в итоге какие методы оставить public, а какие перевести private
    }

    public Set<String> requestToLemmas(String req){
        if (req == null || req.isEmpty()){
            return Collections.emptySet();
        }
        return lemmaService.collectLemmas(req).keySet();
    }

    public List<Site> getSites () {
        List<Site> sites = new ArrayList<>();
        for (SiteConfig siteConfig : sitesList.getSites()){
            String normalisedUrl = IndexingService.normalizeSiteUrl(siteConfig.getUrl());
            Optional<Site> optionalSite = siteRepository.findByUrl(normalisedUrl);
            optionalSite.ifPresent(sites::add);
        }
        return sites;
    }


    public Map<String, Integer> calculateMaxFreq(List<Site> sites){
        for (Site site : sites){
            int pagesCount = pageRepository.countBySite(site);
            Integer freqLimit = Math.round(pagesCount * (1 - SearchConfig.TOP_FREQUENT_LEMMAS_RATIO));
        }
        return new HashMap<>();

    }


}
