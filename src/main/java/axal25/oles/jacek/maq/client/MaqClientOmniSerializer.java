package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseErrorBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseSuccessBodyElement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.List;

@Component
public class MaqClientOmniSerializer {
    private static final Logger logger = LoggerFactory.getLogger(MaqClientOmniSerializer.class);
    private final ObjectMapper objectMapper;
    private final IMaqClient maqClient;

    @Autowired
    public MaqClientOmniSerializer(ObjectMapper objectMapper, IMaqClient maqClient) {
        this.objectMapper = objectMapper;
        this.maqClient = maqClient;
    }

    public MaqSentimentResponse postSentiment(MaqSentimentRequestBody maqSentimentRequestBody) {
        HttpResponse<String> httpResponse = null;
        try {
            httpResponse = maqClient.postSentiment(serializeToJson(maqSentimentRequestBody));
        } catch (MaqUnhandledException e) {
            return MaqSentimentResponse.builder()
                    .errorBody(MaqSentimentResponseErrorBody.builder()
                            .statusCode(500)
                            .message(e.getMessage())
                            .build())
                    .build();
        }

        return MaqSentimentResponse.builder()
                .underlyingResponse(httpResponse)
                .successBody(deserializeFromJsonSuccessBody(httpResponse))
                .errorBody(deSerializeFromJsonErrorBody(httpResponse))
                .build();
    }

    private String serializeToJson(MaqSentimentRequestBody maqSentimentRequestBody) {
        try {
            return objectMapper.writeValueAsString(maqSentimentRequestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
