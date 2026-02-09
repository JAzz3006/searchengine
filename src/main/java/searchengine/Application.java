package searchengine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
//TODO: сделать правильную организацию структуры модели в БД - Liquibase создает, Hibernate проверяет (или отключен)
//TODO: сдлать более обширную инициализацию (проверка права записи, БД, установки краулера и пр.)
//TODO: сделать глобальный Exception Handler
//TODO: подумать над пайплайном для главной страницы - в CrawlingService