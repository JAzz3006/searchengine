package searchengine.util.html;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlParserUtils {
    private HtmlParserUtils() {
    }

    public static String extractTitle(String html) {
        if (html == null || html.isBlank()) return "";
        Document doc = Jsoup.parse(html);
        return doc.title();
    }
}
