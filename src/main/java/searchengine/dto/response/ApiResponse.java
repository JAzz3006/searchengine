package searchengine.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
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
