package searchengine.crawler.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryPolicy {
    private static final Set<String> TRASH_PARAMS = Set.of(
            "utm_source", "utm_medium", "utm_campaign",
            "utm_term", "utm_content",
            "gclid", "fbclid", "yclid",
            "ref", "referrer",
            "session", "sessionid", "phpsessid", "jsessionid"
    );

    private static final int MAX_PARAMS = 5;

//    public static String normalize(String rawQuery) {
//        if (rawQuery == null || rawQuery.isBlank()) return null;
//
//        Map<String, List<String>> params = parse(rawQuery);
//
//        params.entrySet().removeIf(e ->
//                isTrash(e.getKey())
//        );
//
//        if (params.isEmpty() || params.size() > MAX_PARAMS) {
//            return null;
//        }
//
//        return params.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .map(e -> e.getKey() + "=" + e.getValue().get(0))
//                .collect(*-Collectors.joining("&"));
//    }

}

