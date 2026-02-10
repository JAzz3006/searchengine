package searchengine;

import searchengine.crawler.utils.BinaryFilter;
import searchengine.crawler.utils.Normalizer;
import searchengine.crawler.utils.Repairer;
import searchengine.crawler.utils.RubbishFilter;
import searchengine.model.Site;

import java.util.Objects;
import java.util.stream.Stream;

import static searchengine.crawler.utils.SameHost.sameHost;

public class TestPipeline {
    public static void main(String[] args) {
        Site site = new Site();
        site.setUrl("https://dombulgakova.ru/searc h");
        site.setHost("dombulgakova.ru");

        String mainUrlNormalised = Stream.of(site.getUrl())
                .map(String::trim)
                .filter(RubbishFilter::notRubbish)
                .map(Repairer::repair)
                .map(Normalizer::normalise)
                .filter(Objects::nonNull)
                .filter(r -> sameHost(r, site))
                .filter(BinaryFilter::isNotBinary)
                .findFirst()
                .orElse(null);

        System.out.println(mainUrlNormalised);

    }


}
