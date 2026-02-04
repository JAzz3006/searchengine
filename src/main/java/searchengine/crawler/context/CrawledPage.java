package searchengine.crawler.context;
import lombok.AllArgsConstructor;
import searchengine.model.Site;

@AllArgsConstructor
public class CrawledPage {
    Site site;
    String url;
    int statusCode;
    String content;
}