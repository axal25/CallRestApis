package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.http.HttpContainer;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseErrorBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseSuccessBodyElement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

import static org.slf4j.MarkerFactory.getMarker;

@Component
public class MaqOmniSerializer {
    private static final Logger logger = LoggerFactory.getLogger(MaqOmniSerializer.class);
    private final ObjectMapper objectMapper;
    private final CollectionType maqSentimentResponseSuccessBodyElementType;

    @Autowired
    public MaqOmniSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        maqSentimentResponseSuccessBodyElementType = objectMapper.getTypeFactory().constructCollectionType(
                List.class,
                MaqSentimentResponseSuccessBodyElement.class);
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
                    .errorBody(MaqSentimentResponseErrorBody.builder()
                            .statusCode(500)
                            .message(getCauseMessageOrThrowableUnderlyingCauseUsefulMessage(
                                    httpContainer.getCauseMessage(),
                                    httpContainer.getThrowable()))
                            .build())
                    .build();
        }

        return deserializeResponseFromJson(httpContainer.getResponse());
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

    private MaqSentimentResponse deserializeResponseFromJson(HttpResponse<String> httpResponse) {
        List<MaqSentimentResponseSuccessBodyElement> maqSentimentResponseSuccessBodyElements = null;
        MaqSentimentResponseErrorBody maqSentimentResponseErrorBody = null;
        if (httpResponse.statusCode() == 200) {
            try {
                maqSentimentResponseSuccessBodyElements = objectMapper.readValue(
                        httpResponse.body(),
                        maqSentimentResponseSuccessBodyElementType);
            } catch (JsonProcessingException e) {
                String msgFormat = "%s during deserialization of "
                        + List.class.getSimpleName()
                        + "<"
                        + MaqSentimentResponseSuccessBodyElement.class
                        + "> from "
                        + HttpResponse.class.getSimpleName()
                        + "'s Body:\r\n" +
                        "%s";
                logger.error(getMarker("checked exception"),
                        String.format(msgFormat, "{}", "{}"),
                        e.getClass().getSimpleName(),
                        httpResponse.body(),
                        e);
                maqSentimentResponseErrorBody = MaqSentimentResponseErrorBody.builder()
                        .statusCode(500)
                        .message(String.format(msgFormat,
                                e.getClass().getSimpleName(),
                                httpResponse.body()))
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
                    String msgFormat = "%s during deserialization of "
                            + MaqSentimentResponseErrorBody.class.getSimpleName()
                            + " from "
                            + HttpResponse.class.getSimpleName()
                            + "'s Body:\r\n"
                            + "%s";
                    logger.error(getMarker("checked exception"),
                            String.format(msgFormat, "{}", "{}"),
                            e.getClass().getSimpleName(),
                            httpResponse.body(),
                            e);
                    maqSentimentResponseErrorBody = MaqSentimentResponseErrorBody.builder()
                            .statusCode(httpResponse.statusCode())
                            .message(String.format(msgFormat,
                                    e.getClass().getSimpleName(),
                                    httpResponse.body()))
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
