package searchengine.crawler.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class Normalisator {
    public static final Logger log = LoggerFactory.getLogger(Normalisator.class);

    public static String normalise(String ref){
        if (ref == null) return null;
        if (ref.isEmpty()) return null;

        try{
            URI uri = new URI(ref);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null || host.isBlank()) {
                return null;
            }

            scheme = scheme.toLowerCase(Locale.ROOT);
            host = host.toLowerCase(Locale.ROOT);

            int port = uri.getPort();
            String path = uri.getPath();
            if (path == null || path.isBlank()) path = "/";
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            String query = uri.getQuery();

            URI normalized = new URI(
                    scheme,
                    null,
                    host, (port == 80 || port == 443) ? -1 : port,
                    path,
                    query,
                    null
            );
            return normalized.toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}