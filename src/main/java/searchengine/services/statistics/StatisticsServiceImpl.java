package searchengine.services.statistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingService;
import searchengine.util.UrlNormalizer;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexingService indexingService;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(indexingService.isIndexing());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<SiteConfig> sitesList = sites.getSites();
        for (int i = 0; i < sitesList.size(); i++) {
            SiteConfig site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            String normalizedUrl = UrlNormalizer.normalizeSiteUrl(site.getUrl());
            item.setUrl(normalizedUrl);
            Optional<Site> optionalSiteEntity = siteRepository.findByUrl(normalizedUrl);
            int pages = 0;
            int lemmas = 0;
            if (optionalSiteEntity.isPresent()){
                Site siteEntity = optionalSiteEntity.get();
                pages = pageRepository.countBySite(siteEntity);
                item.setPages(pages);
                lemmas = lemmaRepository.countBySite(siteEntity);
                item.setLemmas(lemmas);
                item.setStatus(siteEntity.getStatus().name());
                item.setError(
                        siteEntity.getLastError() == null ? "" : siteEntity.getLastError()
                );
                item.setStatusTime(siteEntity.getStatusTime()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli());
            }else {
                item.setPages(0);
                item.setLemmas(0);
                item.setStatus("INDEXED");
                item.setError("");
                item.setStatusTime(0);
            }

            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}