package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.http.HttpContainer;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseErrorBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseSuccessBodyElement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.List;

@Component
public class MaqOmniSerializer {
    private static final Logger logger = LoggerFactory.getLogger(MaqOmniSerializer.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public MaqOmniSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serializeToJson(MaqSentimentRequestBody maqSentimentRequestBody) throws JsonProcessingException {
        return objectMapper.writeValueAsString(maqSentimentRequestBody);
    }

    public MaqSentimentResponse deserializeFromJson(HttpContainer<String> httpContainer) {
        if (httpContainer.getResponse() == null) {
            if (httpContainer.getThrowable() == null && Strings.isBlank(httpContainer.getCauseMessage())) {
                throw new IllegalStateException("Cause Message and "
                        + Throwable.class.getSimpleName() +
                        " cannot be null or blank at the same time.\r\n" +
                        Throwable.class.getSimpleName() + ": " + httpContainer.getThrowable() + ", " +
                        "Cause Message: " + httpContainer.getCauseMessage() + ".");
            }

            String message = Strings.isBlank(httpContainer.getCauseMessage())
                    ? httpContainer.getThrowable().toString()
                    : httpContainer.getCauseMessage();

            return MaqSentimentResponse.builder()
                    .errorBody(MaqSentimentResponseErrorBody.builder()
                            .statusCode(500)
                            .message(message)
                            .build())
                    .build();
        }

        return deserializeResponseFromJson(httpContainer.getResponse());
    }

    private MaqSentimentResponse deserializeResponseFromJson(HttpResponse<String> httpResponse) {
        List<MaqSentimentResponseSuccessBodyElement> maqSentimentResponseSuccessBodyElements = null;
        MaqSentimentResponseErrorBody maqSentimentResponseErrorBody = null;
        if (httpResponse.statusCode() == 200) {
            try {
                maqSentimentResponseSuccessBodyElements = objectMapper.readValue(
                        httpResponse.body(),
                        objectMapper.getTypeFactory().constructCollectionType(
                                List.class,
                                MaqSentimentResponseSuccessBodyElement.class));
            } catch (JsonProcessingException e) {
                String msgFormat = "Couldn't deserialize "
                        + List.class.getSimpleName()
                        + "<"
                        + MaqSentimentResponseSuccessBodyElement.class
                        + "> from "
                        + HttpResponse.class.getSimpleName()
                        + "'s Body:\r\n" +
                        "%s";
                logger.error(String.format(msgFormat, "{}"), httpResponse, e);
                maqSentimentResponseErrorBody = MaqSentimentResponseErrorBody.builder()
                        .statusCode(httpResponse.statusCode())
                        .message(String.format(msgFormat, httpResponse.body()))
                        .build();
            }
        }
        if (maqSentimentResponseErrorBody == null && httpResponse.statusCode() != 200) {
            if (!httpResponse.body().matches("^\\{[.\\s\\S]*\\}$")) {
                maqSentimentResponseErrorBody = MaqSentimentResponseErrorBody.builder()
                        .statusCode(httpResponse.statusCode())
                        .message(httpResponse.body())
                        .build();
            } else {
                try {
                    maqSentimentResponseErrorBody = objectMapper.readValue(httpResponse.body(), MaqSentimentResponseErrorBody.class);
                } catch (JsonProcessingException e) {
                    String msgFormat = "Couldn't deserialize "
                            + MaqSentimentResponseErrorBody.class.getSimpleName()
                            + " from "
                            + HttpResponse.class.getSimpleName()
                            + "'s Body:\r\n"
                            + "%s";
                    logger.error(String.format(msgFormat, "{}"), httpResponse.body(), e);
                    maqSentimentResponseErrorBody = MaqSentimentResponseErrorBody.builder()
                            .statusCode(httpResponse.statusCode())
                            .message(String.format(msgFormat, httpResponse.body()))
                            .build();
                }
            }
        }

        return MaqSentimentResponse.builder()
                .underlyingResponse(httpResponse)
                .successBody(maqSentimentResponseSuccessBodyElements)
                .errorBody(maqSentimentResponseErrorBody)
                .build();
    }
}
