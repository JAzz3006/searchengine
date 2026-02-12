package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Site;
import searchengine.model.Status;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {
    void deleteByUrl(String url);
    List<Site> getSiteByUrl(String url);
    List<Site> getSitesByStatus(Status status);
    Optional<Site> findByUrl(String url);
}