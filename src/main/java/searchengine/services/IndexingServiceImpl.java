package searchengine.services;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.repositories.SiteRepository;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private volatile boolean indexing = false;
    private final SiteRepository siteRepository;
    private final SitesList sitesList;

    @Override
    public boolean startIndexing() {
        if (indexing){
            return false;
        }
        indexing = true;

        for (SiteConfig siteConfig : sitesList.getSites()){
            siteRepository.deleteByUrl(siteConfig.getUrl());
        }

        //TODO: fork-join crawling

        return true;
    }

    @Override
    public boolean stopIndexing() {
        return false;
    }
}
