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
                ApiResponse.error("Индексация уже запущена");
    }

    @GetMapping("/stopIndexing")
    public ApiResponse stopIndexing(){
        return indexingService.stopIndexing() ?
                ApiResponse.ok() :
                ApiResponse.error("Индексация не запущена");
    }

    @PostMapping("/indexPage")
    public ApiResponse startPageIndexing(@RequestParam String url){
        try {
            indexingService.indexPage(url);
            return ApiResponse.ok();
        }catch (IllegalArgumentException e){
            return ApiResponse.error("Данная страница находится за пределами сайтов,\n" +
                    "указанных в конфигурационном файле");
        }catch (IllegalStateException e){
            return ApiResponse.error("Данная страница недоступна");
        }catch (Exception e){
            return ApiResponse.error("Внутренняя ошибка");
        }
    }
}