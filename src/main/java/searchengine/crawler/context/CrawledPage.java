package searchengine.crawler.context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import searchengine.model.Site;

@AllArgsConstructor
@Getter
public class CrawledPage {
    Site site;
    String pagePath;
    int statusCode;
    String content;
}