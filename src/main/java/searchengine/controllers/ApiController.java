package searchengine.controllers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.response.ApiResponse;
import searchengine.dto.response.SearchResponse;
import searchengine.dto.response.SearchResultItem;
import searchengine.dto.statistics.StatisticsResponse;
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
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ApiResponse> startIndexing(){
        return indexingService.startIndexing() ?
                ResponseEntity.ok(ApiResponse.ok()) :
                ResponseEntity.badRequest().body(ApiResponse.error("Индексация уже запущена"));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ApiResponse> stopIndexing(){
        return indexingService.stopIndexing() ?
                ResponseEntity.ok(ApiResponse.ok()) :
                ResponseEntity.badRequest().body(ApiResponse.error("Индексация не запущена"));
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ApiResponse> startPageIndexing(@RequestParam String url){
        try {
            indexingService.indexPage(url);
            return ResponseEntity
                    .ok(ApiResponse.ok());
        }catch (IllegalArgumentException e){
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Данная страница находится за пределами сайтов, указанных в конфигурационном файле"));
        }catch (IllegalStateException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Данная страница недоступна"));
        }catch (Exception e){
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error("Внутренняя ошибка"));
        }
    }
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search (
            @RequestParam String query,
            @RequestParam (defaultValue = "0") int offset,
            @RequestParam (defaultValue = "20") int limit,
            @RequestParam (required = false) String site){
        if (query == null || query.isBlank()){
            return ResponseEntity
                    .badRequest()
                    .body(SearchResponse.error("Отсутствует поисковый запрос"));
        }
        if (offset < 0){
            return ResponseEntity
                    .badRequest()
                    .body(SearchResponse.error("Отрицательный offset"));
        }
        if (limit <= 0 ){
            return ResponseEntity
                    .badRequest()
                    .body(SearchResponse.error("Лимит должен быть больше 0"));
        }
        if (limit > 100 ){
            limit = 100;
        }

        try{
            List<SearchResultItem> allResults = searchService.startSearch(query, site);
            int totalCount = allResults.size();

            int from = Math.min(totalCount, offset);
            int to = Math.min(from + limit, totalCount);
            List<SearchResultItem> paged = new ArrayList<>(allResults.subList(from, to));


            return ResponseEntity
                    .ok(SearchResponse.success(totalCount, paged));
        }catch(IllegalArgumentException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(SearchResponse.error(e.getMessage()));
        }catch (IllegalStateException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(SearchResponse.error(e.getMessage()));
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SearchResponse.error("Ошибка выполнения поиска"));
        }
    }
}