package searchengine.crawler.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class RubbishFilter {
    private static final Logger log = LoggerFactory.getLogger(RubbishFilter.class);

    private static final Set<String> BAD_PREFIXES = Set.of(
            "mailto:",
            "tel:",
            "javascript:",
            "data:",
            "blob:",
            "file:",
            "ftp:",
            "ws:",
            "wss:",
            "whatsapp:",
            "tg:",
            "viber:",
            "skype:",
            "zoommtg:"
    );

    private static final Set<String> BAD_CONTAINS = Set.of(
            "vk.com",
            "t.me",
            "wa.me",
            "facebook.com",
            "instagram.com",
            "youtube.com",
            "ok.ru"
    );

    public static boolean notRubbish(String ref) {
        if (ref == null) return false;

        String url = ref.trim();
        if (url.isEmpty()) return false;

        // якоря
        if (url.startsWith("#")) return false;

        // javascript:void(0), about:blank
        if (url.equalsIgnoreCase("javascript:void(0)")
                || url.equalsIgnoreCase("about:blank")) {
            return false;
        }

        String lower = url.toLowerCase();

        // плохие схемы
        for (String bad : BAD_PREFIXES) {
            if (lower.startsWith(bad)) {
                return false;
            }
        }

        // соцсети и мессенджеры
        for (String bad : BAD_CONTAINS) {
            if (lower.contains(bad)) {
                return false;
            }
        }

        return true;
    }

//    public static boolean notRubbish(String ref) {
//        if (ref.isBlank()) return false;
//        if (ref.startsWith("mailto:")) return false;
//        if (ref.startsWith("tel:")) return false;
//        return true;
//    }
}
