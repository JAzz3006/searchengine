package searchengine.services.search;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;
import searchengine.config.SearchConfig;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.services.lemma.LemmaService;
import searchengine.util.html.HtmlTextExtractor;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SnippetBuilder {

    private final LemmaService lemmaService;
    private final RussianLuceneMorphology luceneMorphology;

    public String buildSnippet(Page page, List<Lemma> lemmasOfRequest){
        String[] words = getWordsArrayFormHtml(page);
        List<Integer> hits = getMatchIndexes(words, lemmasOfRequest);
        Set<Integer> setOfHits = new HashSet<>(hits);
        if (hits.isEmpty()){
            return buildDefaultSnippet(words);
        }
        int[] densestRange = calcDensestRange(hits, words);
        StringBuilder builder = new StringBuilder();
        builder.append(SearchConfig.SIDES_SYMBOL);

        for (int i = densestRange[0]; i < densestRange[1]; i++) {
            if (i > densestRange[0]) {
                builder.append(' ');
            }
            if (setOfHits.contains(i)) {
                builder.append("<b>")
                        .append(words[i])
                        .append("</b>");
            } else {
                builder.append(words[i]);
            }
        }
        return builder.append(SearchConfig.SIDES_SYMBOL).toString();
    }

    private String[] getWordsArrayFormHtml(Page page){
        String text = HtmlTextExtractor.textExtractor(page.getContent());
        return lemmaService.textSplitter(text);
    }

    private List<Integer> getMatchIndexes (String[] words, List<Lemma> lemmasOfRequest){
        List<Integer> hits = new ArrayList<>();
        Map<String, List<String>> cache = new HashMap<>();
        Set<String> lemmasOfRequestStrings = lemmasOfRequest.stream()
                .map(Lemma::getLemma)
                .collect(Collectors.toSet());

        for (int i = 0; i < words.length; i++){
            String word = words[i].toLowerCase(Locale.ROOT);
            if (!lemmaService.isMeaningfulWord(word)) continue;
            List <String> wordNormForms;
            try {
                wordNormForms = cache.computeIfAbsent(
                        word,
                        luceneMorphology::getNormalForms
                );
            } catch (Exception e) {
                continue;
            }
            for (String lemma : wordNormForms){
                if (lemmasOfRequestStrings.contains(lemma)){
                    hits.add(i);
                    break;
                }
            }
        }
        return hits;
    }
    private int[] calcDensestRange(List<Integer> hits, String[] words){
        int snippetSize = SearchConfig.SNIPPET_SIZE;
        int maxCount = 0;
        int bestStart = 0;
        int left = 0;

        for (int right = 0; right < hits.size(); right++) {
            while (hits.get(right) - hits.get(left) >= snippetSize) {
                left++;
            }
            int count = right - left + 1;
            if (count > maxCount) {
                maxCount = count;
                bestStart = hits.get(left);
            }
        }

        int start = Math.max(0, bestStart - snippetSize / 2);
        int end = start + snippetSize;

        if (end > words.length) {
            end = words.length;
            start = Math.max(0, end - snippetSize);
        }
        return new int[]{start, end};
    }

    private String buildDefaultSnippet(String[] words){
        if (words.length > SearchConfig.SNIPPET_SIZE){
            return arrayToText(words, SearchConfig.SNIPPET_SIZE);
        }else{
            return arrayToText(words, words.length);
        }
    }

    private String arrayToText(String[] words, int textLength){
        StringBuilder builder = new StringBuilder();
        builder.append(SearchConfig.SIDES_SYMBOL);
        for (int i = 0; i < textLength; i++){
            if (i > 0){
                builder.append(' ');
            }
            builder.append(words[i]);
        }
        return builder.append(SearchConfig.SIDES_SYMBOL).toString();
    }
}