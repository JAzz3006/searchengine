package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    Optional<Lemma> findByLemmaAndSite(String lemma, Site site);
    List<Lemma> findAllByLemmaInAndSite(Collection<String> lemmas, Site site);
    int countBySite(Site site);
}