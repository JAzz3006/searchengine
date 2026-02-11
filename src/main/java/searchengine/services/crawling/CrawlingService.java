package searchengine.services.crawling;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.BatchConfig;
import searchengine.crawler.context.CrawlContext;
import searchengine.crawler.engine.CrawlerTask;
import searchengine.crawler.htmlfetcher.PageLoader;
import searchengine.crawler.robots.ResolveRobotsPath;
import searchengine.crawler.robots.ResolveRobotsRules;
import searchengine.crawler.robots.RobotsRules;
import searchengine.crawler.robots.RobotsTxtLoader;
import searchengine.crawler.utils.Normalizer;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageLemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.services.lemma.LemmaService;
import searchengine.services.page.PageBatchWriter;
import searchengine.services.page.PageContentExtractor;
import searchengine.services.site.SiteService;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private static final Logger log = LoggerFactory.getLogger(CrawlingService.class);

    private final List<CrawlContext> activeContexts = new CopyOnWriteArrayList<>();

    private final RobotsTxtLoader robotsTxtLoader;
    private final ResolveRobotsPath resolveRobotsPath;
    private final PageRepository pageRepository;
    private final PageLoader pageLoader;
    private final BatchConfig batchConfig;
    private final PageContentExtractor extractor;
    private final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final PageLemmaRepository pageLemmaRepository;
    private final SiteService siteService;

    public void crawl(Site site) {
        CrawlContext context = null;
        boolean success = false;

        try{
            robotsTxtLoader.getRobotsSaved(site);
            Path robotsPath = resolveRobotsPath.resolve(site);
            ResolveRobotsRules resolver = new ResolveRobotsRules(robotsPath);
            List<String> forbidden = resolver.buildRulesList();
            RobotsRules rules = new RobotsRules(forbidden);

            context = new CrawlContext(rules);
            activeContexts.add(context);

            String mainUrlNormalised = Normalizer.normalise(site.getUrl());
//        String mainUrlNormalised = Stream.of(site.getUrl())
//                .map(String::trim)
//                .map(Repairer::repair)
//                .filter(Objects::nonNull)
//                .map(Normalizer::normalise)
//                .filter(BinaryFilter::isNotBinary)
//                .findFirst()
//                .orElse(null);

            if (mainUrlNormalised == null) {
                log.warn("Root URL rejected after normalization: {}", site.getUrl());
                return;
            }

            if (!context.getVisited().add(mainUrlNormalised)) {
                log.debug("Root URL already visited: {}", mainUrlNormalised);
            }

            PageBatchWriter pageBatchWriter = new PageBatchWriter(
                    context,
                    pageRepository,
                    batchConfig,
                    extractor,
                    lemmaService,
                    lemmaRepository,
                    pageLemmaRepository
            );

            ExecutorService writerExecutor = Executors.newSingleThreadExecutor();
            writerExecutor.submit(pageBatchWriter);

            ForkJoinPool pool = new ForkJoinPool();
            try{
                CrawlerTask rootTask = new CrawlerTask(
                        site,
                        mainUrlNormalised,
                        0,
                        context,
                        pageLoader,
                        siteService);
                pool.invoke(rootTask);
                success = !context.isStopped();;
            }finally {
                pool.shutdown();
                log.info("Crawling finished, waiting for writer...");
                context.finish();
                writerExecutor.shutdown();
                    if (!writerExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                        writerExecutor.shutdownNow();
                    }
            }
        } catch (Exception e) {
            log.error("Crawling failed for site {}", site.getUrl(), e);
            siteService.markFailed(site, e);
            return;
        }finally {
            if (context != null) {
                activeContexts.remove(context);
            }
        }

        if (success) {
            siteService.markIndexed(site);
        }
    }

    public void stopAll() {
        log.info("STOP ALL called, active contexts: {}", activeContexts.size());
        for (CrawlContext context : activeContexts) {
            context.stop();
        }
    }
}