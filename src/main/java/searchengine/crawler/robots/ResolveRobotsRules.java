package searchengine.crawler.robots;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ResolveRobotsRules {
    private static final Logger log = LoggerFactory.getLogger(ResolveRobotsRules.class);

    private final Path robotsPath;

    public boolean robotsTxtIsPresent(){
        return Files.exists(robotsPath) && !Files.isDirectory(robotsPath);
    }

    public List<String> buildRulesList(){
        if (!robotsTxtIsPresent()){
            log.info("Лист исключений не сформирован: файл {} не найден ",
                    robotsPath);
            return List.of();
        }
        ArrayList<String> forbidden = new ArrayList<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(robotsPath);
        } catch (IOException e) {
            log.info("Лист исключений не сформирован - ошибка чтения файла {}: {}", robotsPath, e.getMessage());
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
