package searchengine.services.indexing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.crawler.htmlfetcher.LoadedPage;
import searchengine.crawler.htmlfetcher.PageLoader;
import searchengine.crawler.utils.SameHost;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.crawling.CrawlingService;
import searchengine.services.lemma.LemmaService;
import searchengine.services.page.PageContentExtractor;
import searchengine.services.page.PageIndexingService;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class IndexingService {
    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);
    private final AtomicBoolean indexing = new AtomicBoolean(false);
    private final AtomicInteger activeSites = new AtomicInteger(0);
    private final SitesList sitesList;
    private final SiteService siteService;
    private final PageService pageService;
    private final CrawlingService crawlingService;
    private final PageLoader pageloader;
    private final PageContentExtractor extractor;
    private final LemmaService lemmaService;
    private final PageIndexingService pageIndexingService;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public boolean startIndexing() {
        if (!indexing.compareAndSet(false, true)){
            return false;
        }

        List<SiteConfig> configs = sitesList.getSites();
        if (configs.isEmpty()) {
            indexing.set(false);
            return false;
        }

        for (SiteConfig siteConfig : sitesList.getSites()){
            activeSites.incrementAndGet();
            executor.submit(() -> indexSite(siteConfig));
        }
        return true;
    }

    private void indexSite(SiteConfig siteConfig){
        try {
            String siteUrl = normalizeSiteUrl(siteConfig.getUrl());
            siteService.deleteByUrl(siteUrl);
            Site site = siteService.createIndexingSite(
                    siteUrl,
                    siteConfig.getName());
            crawlingService.crawl(site);
        }finally {
            if (activeSites.decrementAndGet() == 0){
                indexing.set(false);
                log.info("All sites indexed. Indexing finished.");
            }
        }

    }

    public boolean stopIndexing() {
        if (!indexing.get()) return false;
        log.info("User requested stopIndexing()");

        crawlingService.stopAll();

        List<Site> indexingSites = siteService.getSitesByStatus(Status.INDEXING);

        for (Site site : indexingSites){
            siteService.markFailed(site, new RuntimeException("Индексация остановлена пользователем"));
        }
        indexing.set(false);
        return true;
    }

    @Transactional
    public void indexPage(String apiUrl) {
        String normalisedApiUrl = ensureScheme(apiUrl);
        try{
            URI apiUri = new URI(normalisedApiUrl);
            if (apiUri.getScheme() == null) {
                throw new IllegalArgumentException("Page address " + normalisedApiUrl + " must contain schema");
            }
            if (apiUri.getHost() == null) {
                throw new IllegalArgumentException("Page address " + normalisedApiUrl + " must contain host");
            }

            String apiSiteUrl = apiUri.getScheme() + "://" + apiUri.getHost();
            SiteConfig siteConfig = sitesList.getSites().stream()
                    .filter(config -> SameHost.sameHost(apiSiteUrl, config.getUrl()))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException("Page address " + normalisedApiUrl + " is from unknown site")
                    );

            List<Site> sites = siteService.getSiteByUrl(siteConfig.getUrl());
            Site site = sites.isEmpty()
                    ? siteService.createSiteForPageIndexing(siteConfig.getUrl(), siteConfig.getName())
                    : sites.get(0);

            String path = apiUri.getPath();
            if (path == null || path.isBlank()){
                path = "/";
            }
            pageService.deletePageByPathAndSite(path, site);

            LoadedPage loadedPage = pageloader.load(normalisedApiUrl);

            if (loadedPage.getStatusCode() >= 400 || loadedPage.getDoc() == null){
                throw new IllegalStateException("Page returned error status: " + loadedPage.getStatusCode());
            }
                String text = extractor.textExtractor(loadedPage.getHtml());
                Map<String, Integer> lemmas = lemmaService.collectLemmas(text);
                Page page = new Page();
                page.setCode(loadedPage.getStatusCode());
                page.setSite(site);
                page.setContent(loadedPage.getHtml());
                page.setPath(path);
                page = pageService.savePage(page);
                pageIndexingService.saveIndex(page, lemmas);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Wrong page address format");
        }
    }

    public static String ensureScheme(String rawUrl) {
        if (rawUrl == null) return null;

        String url = rawUrl.trim();
        if (url.isEmpty()) return null;
        if (!url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            return "https://" + url;
        }
        return url;
    }

    public static String normalizeSiteUrl(String rawUrl){
        String url = ensureScheme(rawUrl);
        if (rawUrl.endsWith("/")){
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public boolean isIndexing(){
        return indexing.get();
    }
}