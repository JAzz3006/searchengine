package searchengine.services.indexing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;
import searchengine.services.crawling.CrawlingService;
import searchengine.services.site.SiteService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private volatile boolean indexing = false;
    private final SitesList sitesList;
    private final SiteService siteService;
    private  final CrawlingService crawlingService;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public boolean startIndexing() {
        if (indexing){
            return false;
        }
        indexing = true;

        for (SiteConfig siteConfig : sitesList.getSites()){
            executor.submit(() -> indexSite(siteConfig));
        }

        return true;
    }

    private void indexSite(SiteConfig siteConfig){
        siteService.deleteByUrl(siteConfig.getUrl());
        Site site = siteService.createIndexingSite(
                ensureScheme(siteConfig.getUrl()),
                siteConfig.getName());
        crawlingService.crawl(site);
    }

    public void indexPage(String url){

    }

    @Override
    public boolean stopIndexing() {
        return false;
    }

    public static String ensureScheme(String rawUrl) {
        if (rawUrl == null) return null;

        String url = rawUrl.trim();
        if (url.isEmpty()) return null;

        if (!url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            return "https://" + url;
        }
        return url;
    }
}
