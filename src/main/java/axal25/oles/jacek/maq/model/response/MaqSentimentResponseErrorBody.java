package axal25.oles.jacek.maq.model.response;

import lombok.*;

import java.util.List;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class MaqSentimentResponseErrorBody {
    private Integer statusCode;
    private String message;
    private List<MaqSentimentResponseErrorBodyErrorsElement> errors;
}
