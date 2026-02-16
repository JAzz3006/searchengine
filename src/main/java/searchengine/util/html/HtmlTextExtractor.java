package searchengine.util.html;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public final class HtmlTextExtractor {

    private HtmlTextExtractor(){}

    public static String textExtractor(String htmlContent){
        if (htmlContent == null || htmlContent.isBlank()) return "";
        Document doc = Jsoup.parse(htmlContent);
        doc.select("script, style, noscript, iframe, svg").remove();
        doc.select(".ad, .ads, .banner, #footer").remove();
        return doc.text();
    }
}