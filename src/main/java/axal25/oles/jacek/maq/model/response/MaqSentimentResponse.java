package axal25.oles.jacek.maq.model.response;

import lombok.*;

import java.net.http.HttpResponse;
import java.util.List;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class MaqSentimentResponse {
    private Integer statusCode;
    private String message;
    private HttpResponse<String> underlyingResponse;
    private List<MaqSentimentResponseSuccessBodyElement> successes;
    private List<MaqSentimentResponseErrorBodyElement> errors;
}
