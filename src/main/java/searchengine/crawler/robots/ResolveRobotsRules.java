package searchengine.crawler.robots;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ResolveRobotsRules {
    private static Logger log = LoggerFactory.getLogger(ResolveRobotsRules.class);

    public boolean robotsTxtIsPresent(Path path){
        return Files.exists(path) && !Files.isDirectory(path);
    }

    public List<String> buildRulesList(Path path){
        if (!robotsTxtIsPresent(path)){
            log.info("Лист исключений не сформирован: файл {} не найден ",
                    path);
            return new ArrayList<>();
        }
        ArrayList<String> forbidden = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            log.info("Лист исключений не сформирован - ошибка чтения файла {}: {}", path, e.getMessage());
            return new ArrayList<>();
        }
        String pointer = "*";
        boolean isInRightSection = false;
        for (String s : lines){
            s = s.trim();
            String rule = "";
            if (s.isEmpty() || s.startsWith("#")) continue;
            int hash = s.indexOf("#");
            if (hash >= 0){
                s = s.substring(0, hash).trim();
            }
            if (s.isEmpty()) continue;

            String[] keyValue = s.split(":",2);
            if (keyValue.length < 2) continue;
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            if (key.equalsIgnoreCase("user-agent")){
                isInRightSection = value.equals(pointer);
            }

            if (isInRightSection && key.equalsIgnoreCase("disallow")){
                rule = value;
                if (rule.isEmpty()) continue;

                forbidden.add(rule);
            }
        }
        return forbidden;
    }
}
