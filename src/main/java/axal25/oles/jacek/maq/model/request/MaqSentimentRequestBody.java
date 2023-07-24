package axal25.oles.jacek.maq.model.request;

import lombok.*;

import java.util.List;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class MaqSentimentRequestBody {
    private List<MaqSentimentRequestBodyDataElement> data;
}
