package searchengine.crawler.engine;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.CrawlerConfig;
import searchengine.crawler.htmlfetcher.PageLoader;
import searchengine.crawler.utils.Normalisator;
import searchengine.crawler.utils.Repairer;
import searchengine.crawler.utils.RubbishFilter;
import searchengine.model.Site;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CrawlerTask extends RecursiveTask<Void> {
    private static final Semaphore throttle = new Semaphore(CrawlerConfig.MAX_CONCURRENT_REQUESTS);
    private static final int REQUEST_DELAY_MS = CrawlerConfig.REQUEST_DELAY_MS;
    private static final int MAX_DEPTH = CrawlerConfig.MAX_DEPTH;
    private static final AtomicInteger budget = new AtomicInteger(CrawlerConfig.MAX_PAGES_BUDGET);
    private static final Logger log = LoggerFactory.getLogger(CrawlerTask.class);
    private final PageLoader loader = new PageLoader();

    private final Site site;
    private final String url;
    private final int depth;


    @Override
    protected Void compute() {
        if (depth >= MAX_DEPTH){
            return null;
        }

        int remainingBudget = budget.decrementAndGet();
        if (remainingBudget <= 0) {
            if (remainingBudget == 0) log.info("Out of budget");
            else log.debug("Out of budget");
            return null;
        }
        Document doc;
        boolean acquired = false;
        try {
            throttle.acquire();
            acquired = true;
            doc = loader.getDocument(url);
            if (doc == null) {
                log.info("Что-то пошло не так при загрузке по адресу: {}", url);
                return null;
            }

            Thread.sleep(REQUEST_DELAY_MS);

        } catch (InterruptedException e) {
            log.warn("Поток был прерван " + e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        } finally {
            if (acquired) {
                throttle.release();
            }
        }

        //TODO: добавить нормализацию "старшей" url и добавление в visited

        String cssQuery = "abs:href";
        Set<String> children = doc.select("a[href]").stream()
                .map(e -> e.attr(cssQuery))
                .map(String::trim)
                .filter(RubbishFilter::notRubbish)
                .map(Repairer::repair)
                .map(Normalisator::normalise)
                .filter(Objects::nonNull)
                .map(Normalisator::normalise)

                .collect(Collectors.toSet());

        return null;
    }
}
