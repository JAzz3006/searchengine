package searchengine.services.indexing;

public interface IndexingService {
    boolean startIndexing();
    boolean stopIndexing();
    void indexPage(String url);
}
