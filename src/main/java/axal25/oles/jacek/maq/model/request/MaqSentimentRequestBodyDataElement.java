package axal25.oles.jacek.maq.model.request;

import lombok.*;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class MaqSentimentRequestBodyDataElement {
    private String id;
    private String text;
}
