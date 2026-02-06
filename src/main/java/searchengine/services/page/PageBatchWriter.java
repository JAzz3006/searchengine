package searchengine.services.page;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.BatchConfig;
import searchengine.crawler.context.CrawlContext;
import searchengine.crawler.context.CrawledPage;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PageBatchWriter implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(PageBatchWriter.class);

    private final CrawlContext context;
    private final PageRepository pageRepository;
    private final BatchConfig batchConfig;

    private long lastFlushTime = System.currentTimeMillis();

    @Override
    public void run() {
        log.info("PageBatchWriter started");

        List<Page> batch = new ArrayList<>(batchConfig.getPageSize());
        while (!context.isFinished() || !context.getQueue().isEmpty()){
            CrawledPage dto = context.getQueue().poll();
            if (dto != null){
                batch.add(mapToEntity(dto));
            }
            if (batch.size() >= batchConfig.getPageSize() || flushTimeout()){
                flush(batch);
            }
        }
        flush(batch);

        log.info("PageBatchWriter finished");
    }

    private void flush(List<Page> batch){
        if (batch.isEmpty()) return;
        pageRepository.saveAll(batch);
        batch.clear();
        lastFlushTime = System.currentTimeMillis();
    }

    private boolean flushTimeout(){
        long now = System.currentTimeMillis();
        return now - lastFlushTime >= batchConfig.getPageFlushIntervalMs();
    }

    private Page mapToEntity(CrawledPage crawledPage){
        Page page = new Page();
        page.setPath(crawledPage.getUrl());
        page.setSite(crawledPage.getSite());
        page.setContent(crawledPage.getContent());
        page.setCode(crawledPage.getStatusCode());
        return page;
    }
}