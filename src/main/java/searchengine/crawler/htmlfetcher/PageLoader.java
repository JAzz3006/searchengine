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

    public LoadedPage load (String url){
        Connection.Response response = null;
        Document doc = null;
        try{
            response = Jsoup.connect(url)
                    .userAgent(CrawlerConfig.USER_AGENT)
                    .timeout(CrawlerConfig.TIMEOUT)
                    .ignoreHttpErrors(CrawlerConfig.IGNORE_HTTP_ERRORS)
                    .followRedirects(CrawlerConfig.FOLLOW_REDIRECTS)
                    .execute();

            doc = response.statusCode() < 400 ? response.parse() : null;

        }catch (IOException e){
            throw new RuntimeException("Failed to load page: " + url, e);
        }
        return new LoadedPage(
                response.statusCode(),
                response.body(),
                doc
        );
    }

//    public Connection.Response getResponse(String url){
//        Connection.Response response = null;
//        try{
//            response = Jsoup.connect(url)
//                    .userAgent(CrawlerConfig.USER_AGENT)
//                    .timeout(CrawlerConfig.TIMEOUT)
//                    .ignoreHttpErrors(CrawlerConfig.IGNORE_HTTP_ERRORS)
//                    .followRedirects(CrawlerConfig.FOLLOW_REDIRECTS)
//                    .execute();
//        }catch (IOException e){
//            log.warn("Не удалось создать подключение к {}: {}", url, e.getMessage());
//        }
//        return response;
//    }
//
//    public Document getDocument(String url){
//
//        Document doc = null;
//        Connection.Response response = getResponse(url);
//        try {
//            if (response != null){
//                if (response.statusCode() < 400 && response.statusCode() >= 100){
//                    doc = response.parse();
//                }else {
//                    log.warn("Некорректный HTTP ответ от {}:, response code {}", url, response.statusCode());
//                }
//            }else {
//                log.warn("Не удалось получить ответ от {}", url);
//            }
//        }catch (IOException e){
//            log.warn("Ошибка ввода-вывода при обработке ответа от {}: {}", url, e.getMessage());
//        }
//        return doc;
//    }
}