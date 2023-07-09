package axal25.oles.jacek.maq.model.request;

import lombok.*;

import java.util.List;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class MaqSentimentRequestBody {
    private List<MaqSentimentRequestBodyDataElement> data;
}
