package searchengine.services.crawling;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.BatchConfig;
import searchengine.crawler.context.CrawlContext;
import searchengine.crawler.engine.CrawlerTask;
import searchengine.crawler.robots.ResolveRobotsPath;
import searchengine.crawler.robots.ResolveRobotsRules;
import searchengine.crawler.robots.RobotsRules;
import searchengine.crawler.robots.RobotsTxtLoader;
import searchengine.crawler.utils.BinaryFilter;
import searchengine.crawler.utils.Normalizer;
import searchengine.crawler.utils.Repairer;
import searchengine.crawler.utils.RubbishFilter;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.services.page.PageBatchWriter;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import static searchengine.crawler.utils.SameHost.sameHost;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private static final Logger log = LoggerFactory.getLogger(CrawlingService.class);

    private final RobotsTxtLoader robotsTxtLoader;
    private final ResolveRobotsPath resolveRobotsPath;
    private final PageRepository pageRepository;
    private final BatchConfig batchConfig;

    public void crawl(Site site) {
        log.info("host = {}", site.getHost());
        robotsTxtLoader.getRobotsSaved(site);
        Path robotsPath = resolveRobotsPath.resolve(site);
        ResolveRobotsRules resolver = new ResolveRobotsRules(robotsPath);
        List<String> forbidden = resolver.buildRulesList();
        RobotsRules rules = new RobotsRules(forbidden);
        CrawlContext context = new CrawlContext(rules);


        String mainUrlNormalised = Normalizer.normalise(site.getUrl());
//        String mainUrlNormalised = Stream.of(site.getUrl())
//                .map(String::trim)
//                .filter(RubbishFilter::notRubbish)
//                .map(Repairer::repair)
//                .map(Normalizer::normalise)
//                .filter(Objects::nonNull)
//                .filter(r -> sameHost(r, site))
//                .filter(BinaryFilter::isNotBinary)
//                .filter(rules::isAllowed)
//                .findFirst()
//                .orElse(null);

        if (mainUrlNormalised == null) {
            log.warn("Root URL rejected after normalization: {}", site.getUrl());
            return;
        }

        if (!context.getVisited().add(mainUrlNormalised)) {
            log.debug("Root URL already visited: {}", mainUrlNormalised);
        }
//        CrawlerTask task = new CrawlerTask(site, mainUrlNormalised, 0, context);
//        task.runDirect();

        PageBatchWriter pageBatchWriter = new PageBatchWriter(context,pageRepository,batchConfig);
        ExecutorService writerExecutor = Executors.newSingleThreadExecutor();
        writerExecutor.submit(pageBatchWriter);

        ForkJoinPool pool = new ForkJoinPool();
        try{
            CrawlerTask rootTask = new CrawlerTask(site, mainUrlNormalised, 0, context);
            pool.invoke(rootTask);
        }finally {
            pool.shutdown();
            log.info("Crawling finished, waiting for writer...");
            context.finish();
            writerExecutor.shutdown();
            try {
                if (!writerExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    writerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                writerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}