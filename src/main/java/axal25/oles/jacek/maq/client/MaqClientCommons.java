package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.http.HttpContainer;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static axal25.oles.jacek.constant.Constants.CONTENT_TYPE;
import static axal25.oles.jacek.maq.MaqConstants.Endpoints.SENTIMENT;
import static axal25.oles.jacek.maq.MaqConstants.MAQ_API_KEY_NAME;
import static org.slf4j.MarkerFactory.getMarker;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class MaqClientCommons {
    @VisibleForTesting
    static final URI URI_SENTIMENT = URI.create(SENTIMENT);
    private static final Logger logger = LoggerFactory.getLogger(MaqClientCommons.class);
    private final String maqKeyValue;

    @Autowired
    public MaqClientCommons(@Value("${secrets.maq_api_key_value}") String maqKeyValue) {
        this.maqKeyValue = maqKeyValue;
    }

    public HttpContainer.HttpContainerBuilder<String> getContainerBuilder(
            String maqSentimentRequestBodyJson) {
        return HttpContainer.<String>builder()
                .client(getHttpClient())
                .request(HttpRequest.newBuilder()
                        .uri(URI_SENTIMENT)
                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .header(MAQ_API_KEY_NAME, maqKeyValue)
                        .POST(HttpRequest.BodyPublishers.ofString(maqSentimentRequestBodyJson))
                        .build());
    }

    @VisibleForTesting
    HttpClient getHttpClient() {
        return HttpClient.newHttpClient();
    }

    public HttpContainer<String> getHttpContainerFailedSerialization(
            MaqSentimentRequestBody maqSentimentRequestBody,
            Throwable throwable) {
        String msgFormat = "%s during "
                + MaqSentimentRequestBody.class.getSimpleName() +
                " serialization: %s.";
        logger.error(getMarker("checked exception"),
                String.format(msgFormat, "{}", "{}"),
                throwable.getClass().getSimpleName(),
                maqSentimentRequestBody,
                throwable);
        return HttpContainer.<String>builder()
                .throwable(throwable)
                .causeMessage(String.format(msgFormat,
                        throwable.getClass().getSimpleName(),
                        maqSentimentRequestBody))
                .build();
    }

    public HttpContainer<String> getHttpContainerFailedRequest(
            String maqSentimentRequestBodyJson,
            HttpContainer.HttpContainerBuilder<String> containerBuilder,
            Throwable throwable) {
        containerBuilder.throwable(throwable);
        String msgFormat = "%s during "
                + HttpClient.class.getSimpleName()
                + "'s "
                + HttpRequest.class.getSimpleName()
                + ". \r\n"
                + HttpClient.class.getSimpleName() + ": \r\n%s\r\n"
                + HttpRequest.class.getSimpleName() + ": \r\n%s\r\n"
                + HttpRequest.class.getSimpleName() + "'s Body: \r\n%s";
        logger.error(getMarker("checked exception"),
                String.format(msgFormat, "{}", "{}", "{}", "{}"),
                throwable.getClass().getSimpleName(),
                containerBuilder.getClient(),
                containerBuilder.getRequest(),
                maqSentimentRequestBodyJson,
                throwable);
        containerBuilder.causeMessage(String.format(msgFormat,
                throwable.getClass().getSimpleName(),
                containerBuilder.getClient(),
                containerBuilder.getRequest(),
                maqSentimentRequestBodyJson));
        return containerBuilder.build();
    }
}
