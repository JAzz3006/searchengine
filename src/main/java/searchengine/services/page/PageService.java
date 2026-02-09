package searchengine.services.page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;

@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;

    @Transactional
    public void deletePageByPathAndSite(String path, Site site){
        pageRepository.findPageByPathAndSite(path, site)
                .ifPresent(pageRepository::delete);
    }
}