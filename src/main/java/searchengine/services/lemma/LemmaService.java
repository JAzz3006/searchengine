package searchengine.services.lemma;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LemmaService {
    public static final Logger log = LoggerFactory.getLogger(LemmaService.class);

    private static final List<String> SERVICE_PARTS = List.of(
            "ПРЕДЛ",
            "СОЮЗ",
            "ЧАСТ",
            "МЕЖД"
    );

    private final LuceneMorphology luceneMorphology;

    public LemmaService() throws IOException{
        this.luceneMorphology = new RussianLuceneMorphology();
    }

        public HashMap<String, Integer> collectLemmas(String text) {
        String reg1 = "[^\\p{L}]+";
        String[] strings = text.split(reg1);
        return Arrays.stream(strings)
                .filter(word -> !word.isBlank())
                .filter(word -> word.length() > 2)
                .map(word -> word.toLowerCase(Locale.ROOT))
                .filter(this::isMeaningfulWord)
                .flatMap(word -> wordToLemma(word).stream())
                .collect(Collectors.toMap(
                        s -> s,
                        s -> 1,
                        Integer::sum,
                        HashMap::new
                ));
    }

    private boolean isMeaningfulWord(String word) {

            if (!luceneMorphology.checkString(word)) return false;
            for (String f : luceneMorphology.getMorphInfo(word)) {
                for (String part : SERVICE_PARTS) {
                    if (f.contains(part)) return false;
                }
            }
        return true;
    }
    private List<String> wordToLemma(String word){
        return luceneMorphology.getNormalForms(word);
    }
}