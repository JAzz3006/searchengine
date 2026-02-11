package searchengine.config;

public class CrawlerConfig {
    public static final String OUTPUT_PATH = "output";// локация для хранния внешних файлов

    // сетевые настройидентификатор клиента
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/121.0.0.0 Safari/537.36"; //идентификатор клиента
    public static final int TIMEOUT = 10_000; //
    public static final boolean IGNORE_HTTP_ERRORS = true; //
    public static final boolean FOLLOW_REDIRECTS = true; //
    public static final int MAX_CONCURRENT_REQUESTS = 2; // число одновременных сетевых запросов
    public static final int REQUEST_DELAY_MS = 400; // пауза между запросами

    // ограничения обхода
    public static final int MAX_DEPTH = 3; // макс. глубина прохода
    public static final int MAX_PAGES_BUDGET = 5000; //

    // будет ли приложение рассматривать поддомены как "тот же сайт". Например: (ria.ru и news.ria.ru)
    // например: true - поддомены относятся к тому же сайту и включаются в индекс
    public static final boolean SUBDOMAINS_ARE_INCLUDED = false;
    public static final int MAX_QUERY_PARAMS = 5;
    public static final int MAX_QUERY_PARAM_LENGTH = 100;
}
