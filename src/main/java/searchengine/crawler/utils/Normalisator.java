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
        ref = ref.trim();
        if (ref.isEmpty()) return null;

        StringBuilder builder = new StringBuilder();
        try{
            URI uri = new URI(ref);
            String scheme = (uri.getScheme() == null ? "https" : uri.getScheme()).toLowerCase(Locale.ROOT);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                log.warn("Ссылка без хоста отброшена: {}", ref);
                return null;
            }
            int port = uri.getPort();
            String path = uri.getPath();
            if (path == null) path = "";
            if (path.length() > 1 && path.endsWith("/")){
                path = path.substring(0, path.length() - 1);
            }

            builder.append(scheme).append("://").append(host);

            if (port != -1 && port !=80 && port != 443) builder.append(":").append(port);
            builder.append(path);
            return builder.toString();
        } catch (URISyntaxException e) {
            log.warn("Битая ссылка: {}: {}",ref, e.getMessage());
            return null;
        }
    }
}
