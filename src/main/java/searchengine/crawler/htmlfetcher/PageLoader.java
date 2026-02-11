package searchengine.crawler.htmlfetcher;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.CrawlerConfig;
import java.io.IOException;

@RequiredArgsConstructor
@Service
public class PageLoader {

    private static final Logger log = LoggerFactory.getLogger(PageLoader.class);

    public LoadedPage load(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(CrawlerConfig.USER_AGENT)
                    .timeout(CrawlerConfig.TIMEOUT)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .execute();

            int code = response.statusCode();
            String html = response.body();

            Document doc = (code < 400) ? response.parse() : null;
            return new LoadedPage(code, html, doc);

        } catch (IOException e) {
            log.warn("Failed to load page {}", url, e);
            return new LoadedPage(599, "", null);
        }
    }
}