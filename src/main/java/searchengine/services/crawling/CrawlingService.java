package searchengine.services.crawling;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.crawler.engine.CrawlerTask;
import searchengine.crawler.robots.ResolveRobotsPath;
import searchengine.crawler.robots.ResolveRobotsRules;
import searchengine.crawler.robots.RobotsTxtLoader;
import searchengine.model.Site;

import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final RobotsTxtLoader robotsTxtLoader;
    private final ResolveRobotsPath resolveRobotsPath;

    public void crawl(Site site){

        robotsTxtLoader.getRobotsSaved(site);
        ResolveRobotsRules rules = new ResolveRobotsRules();

        ForkJoinPool pool = new ForkJoinPool();

        try{
            CrawlerTask rootTask = new CrawlerTask(site, site.getUrl(), 0, rules);
            pool.invoke(rootTask);
        }finally {
            pool.shutdown();
        }
    }
}