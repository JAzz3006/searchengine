package searchengine.dto.response;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SearchResponse {


    private final boolean result;
    private final int count;
    private final List<SearchResultItem> data;
    private final String error;

}
