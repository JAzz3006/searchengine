package searchengine.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class StartupInitializer {

    private static final Logger log = LoggerFactory.getLogger(StartupInitializer.class);

    @PostConstruct
    public void init(){
        checkDir("logs");
        checkDir(CrawlerConfig.OUTPUT_PATH);
    }

    public void checkDir(String name){
        Path path = Paths.get(name);
        try{
            if (!Files.exists(path) || !Files.isDirectory(path)){
                Files.createDirectories(path);
            }else {
                log.debug("Директория {} существует", path.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалость создать директорию " + path.toAbsolutePath() + " - " + e.getMessage());
        }
    }
}