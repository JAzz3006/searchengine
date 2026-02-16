package searchengine.util.url;

public final class UrlNormalizer {
    private UrlNormalizer(){}

    public static String ensureScheme(String rawUrl) {
        if (rawUrl == null) return null;

        String url = rawUrl.trim();
        if (url.isEmpty()) return null;
        if (!url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            return "https://" + url;
        }
        return url;
    }

    public static String normalizeSiteUrl(String rawUrl){
        String url = ensureScheme(rawUrl);
        if (rawUrl.endsWith("/")){
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
