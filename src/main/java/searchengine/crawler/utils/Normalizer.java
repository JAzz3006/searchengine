package searchengine.crawler.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Normalizer {
    public static final Logger log = LoggerFactory.getLogger(Normalizer.class);
    private static final Set<String> TRASH_PARAMS = Set.of(
            "utm_source", "utm_medium", "utm_campaign",
            "utm_term", "utm_content",
            "gclid", "fbclid", "yclid",
            "ref", "referrer",
            "session", "sessionid", "phpsessid", "jsessionid"
    );

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

    private static String normalizeQuery(String rawQuery){
        if (rawQuery == null || rawQuery.isBlank()) return null;

        Map<String, List<String>> params = parseQuery(rawQuery);

        return "___________";
    }

    public static void main(String[] args) {
        String query = "key1=val1&ke2=val2&key1=val3&debyd&&wild=2";
        Map<String, List<String>> map = parseQuery(query);
        map.forEach((k, v) -> System.out.println(k + " - " + v));
    }

    // Парсим query в Map<key, values>, игнорируя параметры без значений
    private static Map <String, List<String>> parseQuery (String rawQuery){

        Map <String, List<String>> params = new HashMap<>();

        String key = null;
        int start = 0;
        char[] queryAsArray = rawQuery.toCharArray();

        for (int i = 0; i < queryAsArray.length; i++){
            char c = queryAsArray[i];

            if (c == '=' && key == null){
                key = rawQuery.substring(start, i);
                start = i + 1;
            } else if (c == '&' ) {
                if (key != null && start < i){
                    params
                            .computeIfAbsent(key, k -> new ArrayList<>())
                            .add(decode(rawQuery.substring(start, i)));
                }
                key = null;
                start = i + 1;
            }
        }
        if (key != null && start < rawQuery.length()){
            params
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(decode(rawQuery.substring(start)));
        }
        return params;
    }

    private static String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

}