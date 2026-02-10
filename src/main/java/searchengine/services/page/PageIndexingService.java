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

@Service
@RequiredArgsConstructor
public class PageIndexingService {

    private final LemmaRepository lemmaRepository;
    private final PageLemmaRepository pageLemmaRepository;

    @Transactional
    public void saveIndex(Page page, Map<String, Integer> lemmas){
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()){
            Lemma lemma = lemmaRepository.findByLemmaAndSite(entry.getKey(), page.getSite());

            if (lemma == null) {
                lemma = new Lemma();
                lemma.setSite(page.getSite());
                lemma.setLemma(entry.getKey());
                lemma.setFrequency(0);
            }

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