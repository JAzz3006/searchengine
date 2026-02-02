package searchengine.crawler.utils;

import searchengine.config.BinaryExtensions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;

public class BinaryFilter {

    public static boolean isNotBinary(String ref){
        if(ref == null) return false;
        try{
            URI uri = new URI(ref);
            String path = Optional.ofNullable(uri.getPath())
                    .orElse("")
                    .toLowerCase(Locale.ROOT);
            for (String s : BinaryExtensions.EXTENSIONS){
                if (path.endsWith(s)) return false;
            }
            return true;
        }catch (URISyntaxException usex){
            return false;
        }
    }
}
