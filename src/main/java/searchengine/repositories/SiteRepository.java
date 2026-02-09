package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Site;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Long> {
    void deleteByUrl(String url);
    List<Site> getSiteByUrl(String url);
}