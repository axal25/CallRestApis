package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static axal25.oles.jacek.constant.Constants.CONTENT_TYPE;
import static axal25.oles.jacek.maq.MaqConstants.MAQ_API_KEY_NAME;
import static axal25.oles.jacek.maq.client.MaqClient.URI_SENTIMENT;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class MaqAsyncClientUnitTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String maqKeyValueStub = "STUB_MAQ_KEY_VALUE";
    private final Logger logger = (Logger) LoggerFactory.getLogger(MaqAsyncClient.class);
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private MaqOmniSerializer maqOmniSerializerMock;
    private MaqAsyncClient maqAsyncClientMock;
    private HttpClient httpClientMock;

    private static MaqSentimentResponse getUnchecked(CompletableFuture<MaqSentimentResponse> maqSentimentResponseCf) {
        MaqSentimentResponse maqSentimentResponse;
        try {
            maqSentimentResponse = maqSentimentResponseCf.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return maqSentimentResponse;
    }

    private String objectMapperWriteValueAsStringUnchecked(MaqSentimentRequestBody maqSentimentRequestBody) {
        try {
            return objectMapper.writeValueAsString(maqSentimentRequestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        logger.addAppender(listAppender);
        logger.setLevel(Level.ALL);
        listAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        listAppender.start();

        maqOmniSerializerMock = mock(MaqOmniSerializer.class, withSettings()
                .useConstructor(objectMapper)
                .defaultAnswer(CALLS_REAL_METHODS));
        maqAsyncClientMock = mock(MaqAsyncClient.class, withSettings()
                .useConstructor(maqKeyValueStub, maqOmniSerializerMock)
                .defaultAnswer(CALLS_REAL_METHODS));
        httpClientMock = mock(HttpClient.class);
        when(maqAsyncClientMock.getHttpClient()).thenReturn(httpClientMock);
    }

    @AfterEach
    void tearDown() {
        logger.detachAndStopAllAppenders();
    }

    @Test
    void postSentiment_httpClient_send_throwsCheckedException() {
        InterruptedException stubException = new InterruptedException("stub exception message");
        when(httpClientMock.sendAsync(any(), any())).thenReturn(CompletableFuture.failedFuture(stubException));
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder().build();

        MaqSentimentResponse maqSentimentResponse =
                getUnchecked(maqAsyncClientMock.postSentiment(maqSentimentRequestBody));

        assertThat(maqSentimentResponse.getUnderlyingResponse()).isNull();
        assertThat(maqSentimentResponse.getSuccessBody()).isNull();
        assertThat(maqSentimentResponse.getErrorBody()).isNotNull();
        assertThat(maqSentimentResponse.getErrorBody().getStatusCode()).isEqualTo(500);
        HttpRequest expectedHttpRequest = HttpRequest.newBuilder()
                .uri(URI_SENTIMENT)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(MAQ_API_KEY_NAME, maqKeyValueStub)
                .POST(HttpRequest.BodyPublishers.ofString("not displayed here"))
                .build();
        String expectedJsonMaqSentimentRequestBody = objectMapperWriteValueAsStringUnchecked(maqSentimentRequestBody);
        String expectedExceptionMessageFormat = "Exception during HttpClient's HttpRequest. \r\n"
                + "HttpClient: \r\n%s\r\n" +
                "HttpRequest: \r\n%s\r\n" +
                "HttpRequest's Body: \r\n%s";
        String expectedExceptionMessage = String.format(expectedExceptionMessageFormat,
                httpClientMock,
                expectedHttpRequest,
                expectedJsonMaqSentimentRequestBody);
        assertThat(maqSentimentResponse.getErrorBody().getMessage())
                .isEqualTo(expectedExceptionMessage);
        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(listAppender.list.get(0).getMessage())
                .isEqualTo(String.format(expectedExceptionMessageFormat, "{}", "{}", "{}"));
        assertThat(listAppender.list.get(0).getFormattedMessage())
                .isEqualTo(expectedExceptionMessage);
        assertThat(listAppender.list.get(0).getArgumentArray())
                .isEqualTo(new Object[]{
                        httpClientMock,
                        expectedHttpRequest,
                        expectedJsonMaqSentimentRequestBody});
        assertThat(((ThrowableProxy) listAppender.list.get(0).getThrowableProxy()).getThrowable()).isEqualTo(stubException);
        assertThat(listAppender.list.get(0).getMarker().getName()).isEqualTo("checked exception");
    }
}
