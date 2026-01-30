package searchengine.crawler.utils;
import searchengine.config.CrawlerConfig;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Normalizer {

    private static final Set<String> TRASH_PARAMS = Set.of(
            "utm_source", "utm_medium", "utm_campaign",
            "utm_term", "utm_content",
            "gclid", "fbclid", "yclid",
            "ref", "referrer",
            "session", "sessionid", "phpsessid", "jsessionid"
    );

    public static String normalise(String ref){
        if (ref == null || ref.isBlank()) return null;

        try{
            URI uri = new URI(ref);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null || host.isBlank()) {
                return null;
            }

            scheme = scheme.toLowerCase(Locale.ROOT);
            host = host.toLowerCase(Locale.ROOT);
            host = IDN.toASCII(host);

            int port = uri.getPort();
            String path = uri.getPath();
            if (path == null || path.isBlank()) path = "/";
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            String query = normalizeQuery(uri.getQuery()); // query нормализуется отдельно; null = query отброшена политикой

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
        params.entrySet().removeIf(e -> isTrashParam(e.getKey()));

        if (params.isEmpty() || params.size() > CrawlerConfig.MAX_QUERY_PARAMS) {
            return null;
        }

        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue().get(0))// берём первое значение параметра как каноническое
                .collect(Collectors.joining("&"));
    }

    private static boolean isTrashParam(String param){
        if (param == null) return true;
        if(param.length() >CrawlerConfig.MAX_QUERY_PARAM_LENGTH) return true;
        String lower = param.toLowerCase(Locale.ROOT);
        return TRASH_PARAMS.contains(lower)
                || lower.startsWith("utm_");
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