package searchengine.dto.response;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {

    private final boolean result;
    private final Integer count;
    private final List<SearchResultItem> data;
    private final String error;

    public static SearchResponse success(int count, List<SearchResultItem> data){
        return new SearchResponse(true, count, data, null);
    }

    public static SearchResponse error(String message){
        return new SearchResponse(false, null, null, message);
    }

}
