package searchengine.crawler.robots;

import org.springframework.stereotype.Component;
import searchengine.config.CrawlerConfig;
import searchengine.model.Site;

import java.net.IDN;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Component
public class ResolveRobotsPath {
    private String getRobotsFileName(Site site){
        String host = site.getHost();
        if (host == null || host.isBlank()) return "unknown-robots.txt";
        return IDN.toASCII(host.toLowerCase(Locale.ROOT)) + "-robots.txt";
    }

    public Path resolve(Site site){
        return Paths.get(System.getProperty("user.dir"),
                CrawlerConfig.OUTPUT_PATH,
                getRobotsFileName(site));
    }
}
