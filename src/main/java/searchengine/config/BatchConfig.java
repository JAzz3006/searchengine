package searchengine.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "batch")
@Getter
@Setter
public class BatchConfig {
    private int pageSize = 100;
    private long pageFlushIntervalMs = 500;
    private int lemmaSize = 500;
    private long lemmaFlushIntervalMs = 1000;
}