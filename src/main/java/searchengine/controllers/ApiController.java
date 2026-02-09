package searchengine.controllers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.response.ApiResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.statistics.StatisticsService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ApiResponse startIndexing(){
        return indexingService.startIndexing() ?
                ApiResponse.ok() :
                ApiResponse.error("Indexing has already started");
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ApiResponse> startPageIndexing(@RequestParam String url){
        indexingService.indexPage(url);
        return ResponseEntity.ok(ApiResponse.ok());
    }


}
