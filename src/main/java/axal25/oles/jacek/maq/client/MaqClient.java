package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static axal25.oles.jacek.constant.Constants.CONTENT_TYPE;
import static axal25.oles.jacek.maq.MaqConstants.Endpoints.SENTIMENT;
import static axal25.oles.jacek.maq.MaqConstants.MAQ_API_KEY_NAME;
import static org.slf4j.MarkerFactory.getMarker;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class MaqClient implements IMaqClient {
    @VisibleForTesting
    static final URI URI_SENTIMENT = URI.create(SENTIMENT);
    private static final Logger logger = LoggerFactory.getLogger(MaqClient.class);
    private final String maqKeyValue;

    @Autowired
    public MaqClient(@Value("${secrets.maq_api_key_value}") String maqKeyValue) {
        this.maqKeyValue = maqKeyValue;
    }

    @Override
    public HttpResponse<String> postSentiment(String maqRequestBodyJson) throws MaqUnhandledException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI_SENTIMENT)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(MAQ_API_KEY_NAME, maqKeyValue)
                .POST(HttpRequest.BodyPublishers.ofString(maqRequestBodyJson))
                .build();

        HttpClient httpClient = getHttpClient();
        try {
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            String msgFormat = "Exception during "
                    + HttpClient.class.getSimpleName()
                    + "'s "
                    + HttpRequest.class.getSimpleName()
                    + ". \r\n"
                    + HttpClient.class.getSimpleName() + ": \r\n%s\r\n"
                    + HttpRequest.class.getSimpleName() + ": \r\n%s\r\n"
                    + HttpRequest.class.getSimpleName() + "'s Body: \r\n%s";
            logger.error(getMarker("checked exception"),
                    String.format(msgFormat, "{}", "{}", "{}"), httpClient, httpRequest, maqRequestBodyJson,
                    e);

            throw new MaqUnhandledException(String.format(msgFormat, httpClient, httpRequest, maqRequestBodyJson), e);
        }
    }

    @VisibleForTesting
    HttpClient getHttpClient() {
        return HttpClient.newHttpClient();
    }
}
