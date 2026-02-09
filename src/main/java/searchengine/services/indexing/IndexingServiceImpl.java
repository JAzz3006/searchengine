package searchengine.services.indexing;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.crawler.htmlfetcher.LoadedPage;
import searchengine.crawler.htmlfetcher.PageLoader;
import searchengine.crawler.utils.SameHost;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;
import searchengine.services.crawling.CrawlingService;
import searchengine.services.page.PageContentExtractor;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private static final Logger log = LoggerFactory.getLogger(IndexingServiceImpl.class);
    private volatile boolean indexing = false;
    private final SitesList sitesList;
    private final SiteService siteService;
    private final PageService pageService;
    private final CrawlingService crawlingService;
    private final PageLoader pageloader;
    private final PageContentExtractor extractor;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public boolean startIndexing() {
        if (indexing){
            return false;
        }
        indexing = true;

        for (SiteConfig siteConfig : sitesList.getSites()){
            executor.submit(() -> indexSite(siteConfig));
        }

        return true;
    }

    private void indexSite(SiteConfig siteConfig){
        siteService.deleteByUrl(siteConfig.getUrl());
        Site site = siteService.createIndexingSite(
                normalizeSiteUrl(siteConfig.getUrl()),
                siteConfig.getName());
        crawlingService.crawl(site);
    }

    @Override
    public boolean stopIndexing() {
        return false;
    }

    @Override
    @Transactional
    public void indexPage(String apiUrl) {
        String normalisedApiUrl = ensureScheme(apiUrl);
        try{
            URI apiUri = new URI(normalisedApiUrl);
            if (apiUri.getScheme() == null) {
                throw new RuntimeException("Page address " + normalisedApiUrl + " must contain schema");
            }
            if (apiUri.getHost() == null) {
                throw new RuntimeException("Page address " + normalisedApiUrl + " must contain host");
            }

            String apiSiteUrl = apiUri.getScheme() + "://" + apiUri.getHost();
            SiteConfig siteConfig = sitesList.getSites().stream()
                    .filter(config -> SameHost.sameHost(apiSiteUrl, config.getUrl()))
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException("Page address " + normalisedApiUrl + " is from unknown site")
                    );

            List<Site> sites = siteService.getSiteByUrl(siteConfig.getUrl());
            Site site = sites.isEmpty()
                    ? siteService.createIndexingSite(siteConfig.getUrl(), siteConfig.getName())
                    : sites.get(0);

            String path = apiUri.getPath();
            if (path == null || path.isBlank()){
                path = "/";
            }
            pageService.deletePageByPathAndSite(path, site);

            LoadedPage loadedPage = pageloader.load(normalisedApiUrl);

            Document doc;
            if (loadedPage.getStatusCode() <400){
                doc = loadedPage.getDoc();
                extractor.textExtractor(doc.html());
            }else{
                throw new IllegalArgumentException("Page returned error status: " + loadedPage.getStatusCode());
            }

            //        // collect lemmas
            //        // save page
            //        // save index
        } catch (URISyntaxException e) {
            throw new RuntimeException("Wrong page address format");
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
}
