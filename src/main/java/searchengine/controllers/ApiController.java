package searchengine.controllers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SearchConfig;
import searchengine.dto.response.ApiResponse;
import searchengine.dto.response.SearchResponse;
import searchengine.dto.response.SearchResultItem;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.exception.BadRequestException;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;


    @GetMapping("/statistics")
    public StatisticsResponse statistics() {
        return statisticsService.getStatistics();
    }

    @GetMapping("/startIndexing")
    public ApiResponse startIndexing(){
        if (!indexingService.startIndexing()){
            throw new BadRequestException("Индексация уже запущена");
        }
        return ApiResponse.ok();
    }

    @GetMapping("/stopIndexing")
    public ApiResponse stopIndexing(){
        if (!indexingService.stopIndexing()){
            throw new BadRequestException("Индексация не запущена");
        }
        return ApiResponse.ok();
    }

    @PostMapping("/indexPage")
    public ApiResponse startPageIndexing(@RequestParam String url){
            indexingService.indexPage(url);
            return ApiResponse.ok();
    }

    @GetMapping("/search")
    public SearchResponse search (
            @RequestParam String query,
            @RequestParam (defaultValue = "0") int offset,
            @RequestParam (defaultValue = "20") int limit,
            @RequestParam (required = false) String site){
        if (query == null || query.isBlank()){
            throw new BadRequestException("Отсутствует поисковый запрос");
        }
        if (offset < 0){
            throw new BadRequestException("Отрицательный offset");
        }
        if (limit <= 0 ){
            throw new BadRequestException("Лимит должен быть больше 0");
        }
        limit = Math.min(limit, SearchConfig.MAX_LIMIT);

            List<SearchResultItem> allResults = searchService.startSearch(query, site);
            int totalCount = allResults.size();

            int from = Math.min(offset, totalCount);
            int to = Math.min(from + limit, totalCount);
            List<SearchResultItem> paged = new ArrayList<>(allResults.subList(from, to));
            return SearchResponse.success(totalCount, paged);
    }
}