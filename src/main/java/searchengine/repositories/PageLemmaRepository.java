package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.PageLemma;

public interface PageLemmaRepository extends JpaRepository<PageLemma, Long> {
}