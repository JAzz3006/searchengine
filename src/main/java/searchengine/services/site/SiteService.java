package searchengine.services.site;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;

    @Transactional
    public void deleteByUrl(String url){
        siteRepository.deleteByUrl(url);
    }

    @Transactional
    public List<Site> getSiteByUrl(String url){
        return siteRepository.getSiteByUrl(url);
    }

    @Transactional
    public Site createIndexingSite(String url, String name){
        Site site = new Site();
        site.setName(name);
        site.setStatus(Status.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        site.setUrl(url);
        return siteRepository.save(site);
    }

    @Transactional
    public void markIndexed(Site site){
        site.setStatus(Status.INDEXED);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    @Transactional
    public void markFailed(Site site, Exception e){
        site.setStatus(Status.FAILED);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError(e.getMessage());
        siteRepository.save(site);
    }

    @Transactional
    public void updateStatusTime(Site site){
        site.setStatusTime(LocalDateTime.now());
    }
}
