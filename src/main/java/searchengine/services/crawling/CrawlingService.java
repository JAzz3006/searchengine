package searchengine.services.crawling;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.crawler.engine.CrawlerTask;
import searchengine.model.Page;
import searchengine.model.Site;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class CrawlingService {

    public void crawl(Site site){
        ForkJoinPool pool = new ForkJoinPool();
        try{
            CrawlerTask rootTask = new CrawlerTask(site.getUrl(), 0);
            Page root = pool.invoke(rootTask);
        }finally {
            pool.shutdown();
        }
    }
}
