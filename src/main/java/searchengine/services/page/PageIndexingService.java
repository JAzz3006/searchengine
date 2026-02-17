package searchengine.services.page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.PageLemma;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageLemmaRepository;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PageIndexingService {

    private final LemmaRepository lemmaRepository;
    private final PageLemmaRepository pageLemmaRepository;

    @Transactional
    public void saveIndex(Page page, Map<String, Integer> lemmas){
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()){
            Optional<Lemma> optLemma = lemmaRepository.findByLemmaAndSite(entry.getKey(), page.getSite());
            Lemma lemma = optLemma.orElseGet(() ->{
               Lemma newLemma = new Lemma();
               newLemma.setSite(page.getSite());
               newLemma.setLemma(entry.getKey());
               newLemma.setFrequency(0);
               return newLemma;
            });

            lemma.setFrequency(lemma.getFrequency() + 1);
            lemmaRepository.save(lemma);

            PageLemma pageLemma = new PageLemma();
            pageLemma.setPage(page);
            pageLemma.setLemma(lemma);
            pageLemma.setRank(entry.getValue());
            pageLemmaRepository.save(pageLemma);
        }
    }
}