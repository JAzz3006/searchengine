package searchengine.controllers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.response.ApiResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.RequestService;
import searchengine.services.statistics.StatisticsService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final RequestService requestService;


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
    public ResponseEntity<ApiResponse> search (
            @RequestParam String req,
            @RequestParam int offset,
            @RequestParam int limit,
            @RequestParam String siteUrl){
        requestService.startSearch(req, offset, limit, siteUrl);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}