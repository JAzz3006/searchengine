package searchengine.crawler.context;
import lombok.Getter;
import searchengine.config.CrawlerConfig;
import searchengine.crawler.robots.RobotsRules;
import searchengine.services.page.PageService;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class CrawlContext {

    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final RobotsRules rules;
    private final PageService pageService;
    AtomicInteger budget;
    Semaphore throttle;

    public CrawlContext(RobotsRules rules, PageService pageService) {
        this.rules = rules;
        this.pageService = pageService;
        this.budget = new AtomicInteger(CrawlerConfig.MAX_PAGES_BUDGET);
        this.throttle = new Semaphore(CrawlerConfig.MAX_CONCURRENT_REQUESTS);
    }





}