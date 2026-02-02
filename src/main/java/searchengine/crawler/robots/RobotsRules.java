package searchengine.crawler.robots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

public class RobotsRules {
    private static final Logger log = LoggerFactory.getLogger(RobotsRules.class);

    private final List<String> forbidden;

    public RobotsRules(List<String> forbidden) {
        this.forbidden = List.copyOf(forbidden);
    }

    public boolean isAllowed(String ref){

        if (forbidden.isEmpty()){
            return true;
        }

        String target;
        try{
            URI uri = new URI(ref);
            StringBuilder builder = new StringBuilder();
            String path = uri.getPath() == null ? "" : uri.getPath();
            String query = uri.getQuery() == null ? "" : uri.getQuery();
            target = query.isEmpty() ? path : builder.append(path).append("?").append(query).toString();

            String targetLow = target.toLowerCase(Locale.ROOT);

            for (String rule : forbidden){
                String ruleToLow = rule.toLowerCase(Locale.ROOT).trim();
                if (ruleToLow.isBlank()) continue;
                if (!ruleToLow.contains("*") && !ruleToLow.endsWith("$")){
                    if (targetLow.startsWith(ruleToLow)) return false;
                    continue;
                }
                if (!ruleToLow.contains("*") && ruleToLow.endsWith("$")){
                    String base = ruleToLow.substring(0, ruleToLow.length() - 1);
                    if (targetLow.equals(base)) return false;
                    continue;
                }
                if (wildCardUniversal(ruleToLow, targetLow)) return false;
            }
        } catch (URISyntaxException e) {
            log.warn("Что-то пошло не так при обработке ссылки (мы не должны видеть это сообщение никогда) {}: {}",
                    ref,
                    e.getMessage());
            return true;
        }
        return true;
    }

    private boolean wildCardUniversal(String rule, String url){
        if (rule.endsWith("$")){
            rule = rule.substring(0, rule.length() - 1);
        }
        int r = 0;
        int u = 0;
        int startInx = - 1;
        int match = 0;
        while(u < url.length()){
            if (r < rule.length() && rule.charAt(r) == url.charAt(u)){
                r = r + 1;
                u = u + 1;
            }else if (r < rule.length() && rule.charAt(r) == '*') {
                startInx = r;
                r = r + 1;
                match = u;
            }else if (startInx != -1){
                r = startInx + 1;
                match = match + 1;
                u = match;
            }else return false;
        }
        while (r < rule.length() && rule.charAt(r) == '*'){
            r = r + 1;
        }
        return r == rule.length();
    }
}