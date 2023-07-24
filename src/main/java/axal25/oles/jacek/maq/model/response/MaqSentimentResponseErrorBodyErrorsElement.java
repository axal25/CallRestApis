package axal25.oles.jacek.maq.model.response;

import lombok.*;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class MaqSentimentResponseErrorBodyErrorsElement {
    private String property;
    private Long recordNumber;
    private String validator;
    private String value;
    private String message;
}
