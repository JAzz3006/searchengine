package searchengine.crawler.engine;
import searchengine.model.Page;
import java.util.concurrent.RecursiveTask;

public class CrawlerTask extends RecursiveTask<Page> {

    private final String url;
    private final int depth;

    public CrawlerTask(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    @Override
    protected Page compute() {
        return null;
    }
}
