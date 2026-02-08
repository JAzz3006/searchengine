package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.Site;

public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    Lemma findByLemmaAndSite(String lemma, Site site);
}