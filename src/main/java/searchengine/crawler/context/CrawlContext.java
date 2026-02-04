package searchengine.crawler.context;
import lombok.Getter;
import searchengine.config.CrawlerConfig;
import searchengine.crawler.robots.RobotsRules;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class CrawlContext {

    private final Queue<CrawledPage> queue = new ConcurrentLinkedQueue<>();
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean crawlingFinished = new AtomicBoolean(false);

    private final RobotsRules rules;
    AtomicInteger budget;
    Semaphore throttle;

    public CrawlContext(RobotsRules rules) {
        this.rules = rules;
        this.budget = new AtomicInteger(CrawlerConfig.MAX_PAGES_BUDGET);
        this.throttle = new Semaphore(CrawlerConfig.MAX_CONCURRENT_REQUESTS);
    }

    public void enqueue(CrawledPage crawledPage) {
        queue.offer(crawledPage);
    }

    public void finish() {
        crawlingFinished.set(true);
    }

    public boolean isFinished() {
        return crawlingFinished.get();
    }
}