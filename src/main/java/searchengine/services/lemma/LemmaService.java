package searchengine.services.lemma;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.TemporaryText;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Service
public class LemmaService {
    public static final Logger log = LoggerFactory.getLogger(LemmaService.class);

    private static final List<String> SERVICE_PARTS = List.of(
            "ПРЕДЛ",
            "СОЮЗ",
            "ЧАСТ",
            "МЕЖД"
    );

    public static void main(String[] args) {
        collectLemmas(TemporaryText.TEXT);

    }

    public static HashMap<String, Integer> collectLemmas(String text) {
        HashMap<String, Integer> lemmas = new HashMap<>();
        String reg1 = "[^\\p{L}]+";
        String[] strings = text.split(reg1);

        Arrays.stream(strings)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .filter(LemmaService::notServicePart)
                .forEach(System.out::println);

        return new HashMap<>();
    }

    private static boolean notServicePart(String word) {
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            if (!luceneMorph.checkString(word)) return false
            for (String f : luceneMorph.getMorphInfo(word)) {
                for (String part : SERVICE_PARTS) {
                    if (f.contains(part)) return false;
                }
            }
        } catch (WrongCharaterException | IOException e) {
            log.info("Something is wrong with something");
        }
        return true;
    }
}
