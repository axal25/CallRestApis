package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static axal25.oles.jacek.constant.Constants.CONTENT_TYPE;
import static axal25.oles.jacek.maq.MaqConstants.MAQ_API_KEY_NAME;
import static axal25.oles.jacek.maq.client.MaqClient.URI_SENTIMENT;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest
public class MaqClientOmniSerializerUnitTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String maqKeyValueStub = "STUB_MAQ_KEY_VALUE";
    private final Logger logger = (Logger) LoggerFactory.getLogger(MaqClient.class);
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private MaqClient maqClientMock;
    private MaqClientOmniSerializer maqClientOmniSerializerMock;
    private HttpClient httpClientMock;

    @BeforeEach
    void setUp() {
        logger.addAppender(listAppender);
        logger.setLevel(Level.ALL);
        listAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        listAppender.start();

        maqClientMock = mock(MaqClient.class, withSettings()
                .useConstructor(maqKeyValueStub)
                .defaultAnswer(CALLS_REAL_METHODS));
        maqClientOmniSerializerMock = mock(MaqClientOmniSerializer.class, withSettings()
                .useConstructor(objectMapper, maqClientMock)
                .defaultAnswer(CALLS_REAL_METHODS));
        httpClientMock = mock(HttpClient.class);
        when(maqClientMock.getHttpClient()).thenReturn(httpClientMock);
    }

    @AfterEach
    void tearDown() {
        logger.detachAndStopAllAppenders();
    }

    @Test
    void postSentiment_httpClient_send_throwsCheckedException() throws IOException, InterruptedException {
        InterruptedException stubException = new InterruptedException("stub exception message");
        when(httpClientMock.send(any(), any())).thenThrow(stubException);
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder().build();

        MaqSentimentResponse maqSentimentResponse = maqClientOmniSerializerMock.postSentiment(maqSentimentRequestBody);

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
        String expectedJsonMaqSentimentRequestBody = objectMapper.writeValueAsString(maqSentimentRequestBody);
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
