package searchengine.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private boolean result;
    private String error;

    public static ApiResponse ok(){
        return new ApiResponse(true, null);
    }

    public static ApiResponse error(String message){
        return new ApiResponse(false, message);
    }
}
