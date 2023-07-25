package axal25.oles.jacek.maq.model;

import axal25.oles.jacek.http.HttpContainer;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.Objects;

import static org.slf4j.MarkerFactory.getMarker;

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
        Preconditions.checkNotNull(httpContainer,
                "%s argument cannot be null.", HttpContainer.class.getSimpleName());
        if (httpContainer.getResponse() == null) {
            Preconditions.checkArgument(
                    Strings.isNotBlank(httpContainer.getCauseMessage())
                            || Objects.nonNull(httpContainer.getThrowable()),
                    "%s argument's Cause Message and %s cannot be null or blank at the same time.\r\n" +
                            "Cause Message: %s, %s: %s.",
                    HttpContainer.class.getSimpleName(),
                    Throwable.class.getSimpleName(),
                    httpContainer.getCauseMessage(),
                    Throwable.class.getSimpleName(),
                    httpContainer.getThrowable());


            return MaqSentimentResponse.builder()
                    .statusCode(500)
                    .message(getCauseMessageOrThrowableUnderlyingCauseUsefulMessage(
                            httpContainer.getCauseMessage(),
                            httpContainer.getThrowable()))
                    .underlyingResponse(null)
                    .successes(null)
                    .errors(null)
                    .build();
        }

        return deserializeHttpResponse(httpContainer.getResponse());
    }

    private String getCauseMessageOrThrowableUnderlyingCauseUsefulMessage(String causeMessage, Throwable throwable) {
        if (Strings.isNotBlank(causeMessage)) {
            return causeMessage;
        }

        return getThrowableUnderlyingCauseUsefulMessage(throwable);
    }

    private String getThrowableUnderlyingCauseUsefulMessage(Throwable throwable) {
        String usefulMessage = null;
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            if (Strings.isNotBlank(cause.getMessage())) {
                usefulMessage = cause.getMessage();
            }
            throwable = cause;
        }

        return Strings.isNotBlank(usefulMessage)
                ? usefulMessage
                : throwable.getClass().getSimpleName();
    }

    private MaqSentimentResponse deserializeHttpResponse(HttpResponse<String> httpResponse) {
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(httpResponse.body());
        } catch (JsonProcessingException ignored) {
        }
        boolean bodyIsJsonObject = jsonNode != null && jsonNode.isObject();
        boolean bodyIsArrayOfJsonObjects = jsonNode != null && jsonNode.isArray();

        if (!bodyIsJsonObject && !bodyIsArrayOfJsonObjects) {
            return MaqSentimentResponse.builder()
                    .statusCode(httpResponse.statusCode())
                    .message(httpResponse.body())
                    .underlyingResponse(httpResponse)
                    .successes(null)
                    .errors(null)
                    .build();
        }
        String httpResponseBody = httpResponse.statusCode() == 200 && bodyIsArrayOfJsonObjects
                ? String.format("{\"successes\": %s}", httpResponse.body())
                : httpResponse.body();
        try {
            return objectMapper.readValue(
                            httpResponseBody,
                            MaqSentimentResponse.class)
                    .toBuilder()
                    .statusCode(httpResponse.statusCode())
                    .underlyingResponse(httpResponse)
                    .build();
        } catch (JsonProcessingException e) {
            String msgFormat = "%s during deserialization of "
                    + MaqSentimentResponse.class.getSimpleName()
                    + " from "
                    + HttpResponse.class.getSimpleName()
                    + "'s Body:\r\n" +
                    "%s";
            logger.error(getMarker("checked exception"),
                    String.format(msgFormat, "{}", "{}"),
                    e.getClass().getSimpleName(),
                    httpResponseBody,
                    e);
            return MaqSentimentResponse.builder()
                    .statusCode(500)
                    .message(String.format(msgFormat,
                            e.getClass().getSimpleName(),
                            httpResponseBody))
                    .underlyingResponse(httpResponse)
                    .successes(null)
                    .errors(null)
                    .build();
        }
    }

}
