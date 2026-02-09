package searchengine.crawler.htmlfetcher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;

@RequiredArgsConstructor
@Getter
public class LoadedPage {
    private final int statusCode;
    private final String html;
    private final Document doc;
}
