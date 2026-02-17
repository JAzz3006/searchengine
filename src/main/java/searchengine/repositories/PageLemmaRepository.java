package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.PageLemma;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PageLemmaRepository extends JpaRepository<PageLemma, Long> {
    List<PageLemma> findAllByLemmaId(Long lemmaId);
    @Query("select distinct pl.page from PageLemma pl where pl.lemma.id = :lemmaId")
    Set<Page> findPagesByLemmaId(@Param("lemmaId") Long lemmaId);
    Optional<PageLemma> findPageLemmaByPageIdAndLemmaId(Long lemmaId, Long pageId);
    List<PageLemma> findAllByPage_IdInAndLemma_IdIn(Collection<Long> pageIds, Collection<Long> lemmaIds);
}