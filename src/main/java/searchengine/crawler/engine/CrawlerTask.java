package searchengine.crawler.engine;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.CrawlerConfig;
import searchengine.crawler.context.CrawlContext;
import searchengine.crawler.htmlfetcher.PageLoader;
import searchengine.crawler.utils.BinaryFilter;
import searchengine.crawler.utils.Normalizer;
import searchengine.crawler.utils.Repairer;
import searchengine.crawler.utils.RubbishFilter;
import searchengine.model.Site;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import static searchengine.crawler.utils.SameHost.sameHost;

@RequiredArgsConstructor
public class CrawlerTask extends RecursiveAction {
    private static final Logger log = LoggerFactory.getLogger(CrawlerTask.class);

    private static final int REQUEST_DELAY_MS = CrawlerConfig.REQUEST_DELAY_MS;
    private static final int MAX_DEPTH = CrawlerConfig.MAX_DEPTH;
    private final PageLoader loader = new PageLoader();

    private final Site site;
    private final String url;
    private final int depth;
    private final CrawlContext context;

    @Override
    protected void compute() {
        if (depth >= MAX_DEPTH){
            return;
        }

        int remainingBudget = context.getBudget().decrementAndGet();
        if (remainingBudget <= 0) {
            if (remainingBudget == 0) log.info("Out of budget");
            else log.debug("Out of budget");
            return;
        }
        Document doc;
        Connection.Response response;
        boolean acquired = false;
        try {
            context.getThrottle().acquire();
            acquired = true;
            response = loader.getResponse(url);
            doc = loader.getDocument(url);
            if (doc == null) {
                log.info("Что-то пошло не так при загрузке по адресу: {}", url);
                return;
            }

            Thread.sleep(REQUEST_DELAY_MS);

        } catch (InterruptedException e) {
            log.warn("Поток был прерван " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        } finally {
            if (acquired) {
                context.getThrottle().release();
            }
        }

        context.getPageService().savePage(response.statusCode(), site, url, doc);

        Set<String> children = doc.select("a[href]").stream()
                .map(e -> e.attr("abs:href"))
                .map(String::trim)
                .filter(RubbishFilter::notRubbish)
                .map(Repairer::repair)
                .map(Normalizer::normalise)
                .filter(Objects::nonNull)
                .filter(r -> sameHost(r, site))
                .filter(BinaryFilter::isNotBinary)
                .filter(context.getRules()::isAllowed)
                .filter(context.getVisited()::add)
                .collect(Collectors.toSet());

        List<CrawlerTask> tasks = new ArrayList<>();

        Iterator<String> iterator = children.iterator();
        while (iterator.hasNext()) {
            String child = iterator.next();
            CrawlerTask task = new CrawlerTask(site, child, depth + 1, context);
            if (iterator.hasNext()) {
                task.fork();
                tasks.add(task);
            } else {
                task.compute();
            }
        }

        for (CrawlerTask task : tasks) {
            task.join();
        }
    }
}