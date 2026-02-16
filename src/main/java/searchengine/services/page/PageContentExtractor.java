//package searchengine.services.page;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.springframework.stereotype.Service;
//
//@Service
//public class PageContentExtractor {
//    public String textExtractor(String htmlContent){
//        if (htmlContent == null || htmlContent.isBlank()) return "";
//        Document doc = Jsoup.parse(htmlContent);
//        doc.select("script, style, noscript, iframe, svg").remove();
//        doc.select(".ad, .ads, .banner, #footer").remove();
//        return doc.text();
//    }
//}
