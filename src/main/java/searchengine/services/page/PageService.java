package searchengine.services.page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.PageLemma;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;

@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Transactional
    public void deletePageByPathAndSite(String path, Site site){
        pageRepository.findPageByPathAndSite(path, site)
                .ifPresent(this::deletePageWithLemmas);
    }

    private void deletePageWithLemmas(Page page){
        for (PageLemma pl : page.getPageLemmasOfPage()){
            Lemma lemma = pl.getLemma();
            int newFreq = lemma.getFrequency() - 1;
            if (newFreq <= 0){
                lemmaRepository.delete(lemma);
            }else {
                lemma.setFrequency(newFreq);
            }
        }
        pageRepository.delete(page);
    }

    public Page savePage(Page page){
        return pageRepository.save(page);
    }
}