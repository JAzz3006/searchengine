package searchengine.crawler.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RubbishFilter {
    private static final Logger log = LoggerFactory.getLogger(RubbishFilter.class);

    public static boolean notRubbish(String ref) {
        if (ref.isBlank()) return false;
        if (ref.startsWith("mailto:")) return false;
        if (ref.startsWith("tel:")) return false;
        return true;
    }
}
