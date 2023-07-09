package axal25.oles.jacek.maq.model.response;

import lombok.*;

import java.math.BigDecimal;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class MaqSentimentResponseSuccessBodyElement {
    private String id;
    private BigDecimal sentiment;
}
