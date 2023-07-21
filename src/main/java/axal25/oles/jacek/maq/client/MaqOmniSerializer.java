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

    public String serializeToJson(MaqSentimentRequestBody maqSentimentRequestBody) {
        try {
            return objectMapper.writeValueAsString(maqSentimentRequestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public MaqSentimentResponse deserializeFromJson(HttpContainer<String> httpContainer) {
        if(httpContainer.getResponse() == null) {
            return getErrorBodyFromThrowableOrCauseMessage(httpContainer);
        }

        return MaqSentimentResponse.builder()
                .underlyingResponse(httpContainer.getResponse())
                .successBody(deserializeFromJsonSuccessBody(httpContainer.getResponse()))
                .errorBody(deSerializeFromJsonErrorBody(httpContainer.getResponse()))
                .build();
    }

    private MaqSentimentResponse getErrorBodyFromThrowableOrCauseMessage(HttpContainer<String> httpContainer) {
        if(httpContainer.getResponse() != null) {
            throw new IllegalStateException(HttpResponse.class.getSimpleName() + " must be null.");
        }

        if(httpContainer.getThrowable() == null || Strings.isBlank(httpContainer.getCauseMessage())) {
            throw new IllegalStateException("Cause Message and "
                    + Throwable.class.getSimpleName() +
                    " both at the same time cannot be null.");
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

    private List<MaqSentimentResponseSuccessBodyElement> deserializeFromJsonSuccessBody(HttpResponse<String> httpResponse) {
        if (httpResponse.statusCode() != 200) {
            return null;
        }

        try {
            return objectMapper.readValue(
                    httpResponse.body(),
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class,
                            MaqSentimentResponseSuccessBodyElement.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private MaqSentimentResponseErrorBody deSerializeFromJsonErrorBody(HttpResponse<String> httpResponse) {
        if (httpResponse.statusCode() == 200) {
            return null;
        }

        if (!httpResponse.body().matches("^\\{[.\\s\\S]*\\}$")) {
            return getErrorBodyFromUnDeSerializable(httpResponse);
        }

        try {
            return objectMapper.readValue(httpResponse.body(), MaqSentimentResponseErrorBody.class);
        } catch (JsonProcessingException e) {
            String msgFormat = "Couldn't deserialize "
                    + MaqSentimentResponseErrorBody.class.getSimpleName()
                    + " from "
                    + HttpResponse.class.getSimpleName()
                    + "'s Body: \r\n {}";
            logger.error(msgFormat, httpResponse.body(), e);
            return getErrorBodyFromUnDeSerializable(httpResponse);
        }
    }

    private MaqSentimentResponseErrorBody getErrorBodyFromUnDeSerializable(HttpResponse<String> httpResponse) {
        return MaqSentimentResponseErrorBody.builder()
                .statusCode(httpResponse.statusCode())
                .message(httpResponse.body())
                .build();
    }
}
