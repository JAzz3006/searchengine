package searchengine.crawler.robots;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.model.Site;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class RobotsTxtLoader {
    public static final Logger log = LoggerFactory.getLogger(RobotsTxtLoader.class);

    private final ResolveRobotsPath resolveRobotsPath;

    public void getRobotsSaved(Site site) {

        Path targetPath = resolveRobotsPath.resolve(site);

        try {
            Files.createFile(targetPath);
        } catch (FileAlreadyExistsException e) {
            log.debug("Файл {} уже существует", targetPath);
            return;
        } catch (IOException e) {
            log.error("Не удалось создать файл robots.txt", e);
            return;
        }

        String bot = "robots.txt";
        try {
            URI uri = new URI(site.getUrl());
            Connection.Response resp = Jsoup.connect(uri.resolve(bot).toString())
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .execute();
            if (resp.statusCode() == 200) {
                try (BufferedWriter writer = Files.newBufferedWriter(targetPath)) {
                    writer.write(resp.body());
                    log.info("Файл успешно записан: {}", targetPath);

                } catch (IOException e) {
                    log.info("ошибка записи в файл {}: {}", targetPath, e.getMessage());
                }
            } else {
                log.info("robots.txt отсутствует ({}), считаем доступным всё: {}",
                        resp.statusCode(),
                        site.getUrl());
            }
        } catch (IOException e) {
            log.info("Ошибка при загрузке {} с {}: {}", bot, site.getUrl(), e.getMessage());
        } catch (URISyntaxException e) {
            log.info("Проблема со ссылкой {} - {}", site.getUrl(), e.getMessage());
        }
    }
}