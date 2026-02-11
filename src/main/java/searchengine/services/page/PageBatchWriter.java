package searchengine.services.page;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.BatchConfig;
import searchengine.crawler.context.CrawlContext;
import searchengine.crawler.context.CrawledPage;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.PageLemma;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageLemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.services.lemma.LemmaService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PageBatchWriter implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(PageBatchWriter.class);

    private final CrawlContext context;
    private final PageRepository pageRepository;
    private final BatchConfig batchConfig;
    private final PageContentExtractor extractor;
    private  final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final PageLemmaRepository pageLemmaRepository;

    private long pageLastFlushTime = System.currentTimeMillis();

    @Override
    public void run() {
        log.info("PageBatchWriter started");

        List<Page> batch = new ArrayList<>(batchConfig.getPageSize());
        Map<Page, Map<String, Integer>> pendingIndexes = new HashMap<>();

        while (true){
            if ((context.isStopped() || context.isFinished())
                    && context.getQueue().isEmpty()){
                log.info("Writer sees STOP signal");
                break;
            }

            CrawledPage dto = context.getQueue().poll();
            if (dto == null){
                sleepShort();
                continue;
            }
            if (dto.getStatusCode() >= 400){
                continue;
            }

            try {
                String text = extractor.textExtractor(dto.getContent());
                Map<String, Integer> lemmas = lemmaService.collectLemmas(text);
                Page page = mapToEntity(dto);
                batch.add(page);
                pendingIndexes.put(page, lemmas);

                if (batch.size() >= batchConfig.getPageSize() || pageFlushTimeout()) {
                    flushAndIndex(batch, pendingIndexes);
                }

            }catch (Exception e){
                log.error("Indexing failed for page {} :", dto.getUrl(), e);
            }
        }
        flushAndIndex(batch, pendingIndexes);
        log.info("PageBatchWriter finished");
    }

    public void saveIndex(Page page, Map<String, Integer> lemmas){
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()){
            Lemma lemma = lemmaRepository.findByLemmaAndSite(entry.getKey(), page.getSite());

            if (lemma == null) {
                lemma = new Lemma();
                lemma.setSite(page.getSite());
                lemma.setLemma(entry.getKey());
                lemma.setFrequency(0);
            }

            lemma.setFrequency(lemma.getFrequency() + 1);
            lemmaRepository.save(lemma);

            PageLemma pageLemma = new PageLemma();
            pageLemma.setPage(page);
            pageLemma.setLemma(lemma);
            pageLemma.setRank(entry.getValue());
            pageLemmaRepository.save(pageLemma);
        }
    }

    private void flushAndIndex(List<Page> batch, Map<Page, Map<String, Integer>> pendingIndexes){
        if (batch.isEmpty()) return;

        pageRepository.saveAll(batch);
        pageRepository.flush();   // !!!

        for (Page page : batch) {
            Map<String, Integer> lemmas = pendingIndexes.get(page);
            if (lemmas != null) saveIndex(page, lemmas);
        }
        batch.clear();
        pendingIndexes.clear();
        pageLastFlushTime = System.currentTimeMillis();
    }

    private boolean pageFlushTimeout(){
        long now = System.currentTimeMillis();
        return now - pageLastFlushTime >= batchConfig.getPageFlushIntervalMs();
    }

    private void sleepShort(){
        try{
            Thread.sleep(10);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
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