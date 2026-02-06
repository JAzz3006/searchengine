package searchengine.crawler.utils;
import searchengine.config.CrawlerConfig;
import searchengine.model.Site;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class SameHost {

    public static boolean sameHost(String u1, Site site) {
        if (u1 == null) return false;
        try {
            URI uri1 = new URI(u1);
            String h1 = uri1.getHost();
            if (h1 == null) return false;
            h1 = IDN.toASCII(h1.toLowerCase(Locale.ROOT));
            if (CrawlerConfig.SUBDOMAINS_ARE_INCLUDED){
                return h1.equals(site.getHost()) || h1.endsWith("." + site.getHost());
            }else{
                return h1.equals(site.getHost());
            }
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static boolean preSameHost(String ref, String siteHost) {
        if (ref == null) return false;
        String lower = ref.toLowerCase(Locale.ROOT);
        String host = siteHost.toLowerCase(Locale.ROOT);

        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return lower.startsWith("http://" + host)
                    || lower.startsWith("https://" + host);
        }
        return true;
    }
}