package searchengine.services.lemma;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LemmaService {
    public static final Logger log = LoggerFactory.getLogger(LemmaService.class);

    private static final List<String> SERVICE_PARTS = List.of(
            "ПРЕДЛ",
            "СОЮЗ",
            "ЧАСТ",
            "МЕЖД"
    );

    private final RussianLuceneMorphology luceneMorphology;

        public HashMap<String, Integer> collectLemmas(String text) {
            return Arrays.stream(textSplitter(text))
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

    public String[] textSplitter (String text){
        String reg1 = "[^\\p{L}]+";
        return text.split(reg1);
    }

    public boolean isMeaningfulWord(String word) {
        try {
            if (!luceneMorphology.checkString(word)) return false;

            for (String f : luceneMorphology.getMorphInfo(word)) {
                for (String part : SERVICE_PARTS) {
                    if (f.contains(part)) return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private List<String> wordToLemma(String word){
        return luceneMorphology.getNormalForms(word);
    }
}