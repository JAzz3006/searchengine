package searchengine.services.indexing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.repositories.SiteRepository;
import searchengine.services.site.SiteService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private volatile boolean indexing = false;
    private final SitesList sitesList;
    private final SiteService siteService;
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
        siteService.createIndexingSite(siteConfig.getUrl(), siteConfig.getName());


    }

    @Override
    public boolean stopIndexing() {
        return false;
    }
}
