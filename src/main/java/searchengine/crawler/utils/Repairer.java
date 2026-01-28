package searchengine.crawler.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Repairer {
    private static final Logger log = LoggerFactory.getLogger(Repairer.class);

    public static String repair(String ref){
        try{
            URI uri = new URI(ref);
        } catch (URISyntaxException e) {
            log.warn("Ссылка {} не в порядке, пробуем починить", ref);
            String fixed = softFixUrl(ref);
            if (fixed == null || fixed.isBlank()) {
                log.warn("Ссылку не починить, выбрасываем {}: (softFixUrl вернул null/blank)", ref);
                return null;
            }
            try {
                new URI(fixed);
                log.info("Ссылку починил: {}", fixed);
                return fixed;
            } catch (URISyntaxException ex) {
                log.warn("Ссылку не починить, выбрасываем {}: {}", ref, ex.getMessage());
                return null;
            }
        }
        return ref;
    }

    private static String softFixUrl(String ref) {
        String marker = "#:~:text=";
        int idx = ref.indexOf(marker);
        if (idx >= 0) {
            ref = ref.substring(0, idx);
        }
        ref = ref.replace(" ", "%20")
                .replace("{", "%7B")
                .replace("}", "%7D");

        try {
            URL url = new URL(ref);
            String protocol = url.getProtocol();
            String userInfo = url.getUserInfo();
            String host = url.getHost();
            int port = url.getPort();
            if (host == null || host.isBlank()) {
                return null;
            }
            String asciiHost = IDN.toASCII(host);
            String path = encodePath(url.getPath());
            String query = encodeQuery(url.getQuery());
            URI fixed = new URI(protocol, userInfo, asciiHost, port, path, query, null);
            return fixed.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String encodePath(String path) {
        if (path == null || path.isEmpty()) return path;

        StringBuilder sb = new StringBuilder(path.length());
        for (char c : path.toCharArray()) {
            if (isUnreserved(c) || c == '/') {
                sb.append(c);
            } else {
                byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append(String.format("%%%02X", b & 0xFF));
                }
            }
        }
        return sb.toString();
    }

    private static String encodeQuery(String query) {
        if (query == null || query.isEmpty()) return query;

        StringBuilder sb = new StringBuilder(query.length());
        for (char c : query.toCharArray()) {
            if (isUnreserved(c) || c == '=' || c == '&') {
                sb.append(c);
            } else {
                byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append(String.format("%%%02X", b & 0xFF));
                }
            }
        }
        return sb.toString();
    }

    private static boolean isUnreserved(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '-' || c == '.' || c == '_' || c == '~';
    }
}