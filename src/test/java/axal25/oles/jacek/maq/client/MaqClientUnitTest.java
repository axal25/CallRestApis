package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.maq.model.MaqOmniSerializer;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import axal25.oles.jacek.util.CurrentThreadExecutor;
import axal25.oles.jacek.util.ThreadLocalListAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static axal25.oles.jacek.constant.Constants.CONTENT_TYPE;
import static axal25.oles.jacek.maq.MaqConstants.MAQ_API_KEY_NAME;
import static axal25.oles.jacek.maq.client.MaqClientCommons.URI_SENTIMENT;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class MaqClientUnitTest {
    private static ThreadLocalListAppender listAppender;
    private static Logger logger;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String maqKeyValueStub = "STUB_MAQ_KEY_VALUE";
    private Executor executor;
    private MaqClientCommons maqClientCommonsMock;
    private MaqOmniSerializer maqOmniSerializerMock;
    private MaqClient maqClientMock;
    private HttpClient httpClientMock;

    @BeforeAll
    static void beforeAll() {
        listAppender = new ThreadLocalListAppender();
        logger = ThreadLocalListAppender.getLogger(MaqClientCommons.class);
    }

    @BeforeEach
    void setUp() {
        executor = new CurrentThreadExecutor();
        maqClientCommonsMock = mock(MaqClientCommons.class, withSettings()
                .useConstructor(maqKeyValueStub, executor)
                .defaultAnswer(CALLS_REAL_METHODS));
        httpClientMock = mock(HttpClient.class);
        when(maqClientCommonsMock.getHttpClient()).thenReturn(httpClientMock);
        maqOmniSerializerMock = mock(MaqOmniSerializer.class, withSettings()
                .useConstructor(objectMapper)
                .defaultAnswer(CALLS_REAL_METHODS));
        maqClientMock = mock(MaqClient.class, withSettings()
                .useConstructor(maqClientCommonsMock, maqOmniSerializerMock)
                .defaultAnswer(CALLS_REAL_METHODS));
        listAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
    }

    @AfterEach
    void tearDown() {
        listAppender.stopAndDetach(logger);
    }

    @Test
    void postSentiment_maqOmniSerializer_serializeToJson_throwsCheckedException() {
        JsonProcessingException stubException = mock(JsonProcessingException.class, withSettings()
                .useConstructor("stub exception message")
                .defaultAnswer(CALLS_REAL_METHODS));
        assertDoesNotThrow(() ->
                when(maqOmniSerializerMock.serializeToJson(any())).thenThrow(stubException));
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder().build();

        MaqSentimentResponse maqSentimentResponse =
                maqClientMock.postSentiment(maqSentimentRequestBody);

        assertThat(maqSentimentResponse.getUnderlyingResponse()).isNull();
        assertThat(maqSentimentResponse.getSuccesses()).isNull();
        assertThat(maqSentimentResponse.getStatusCode()).isEqualTo(500);
        assertThat(maqSentimentResponse.getErrors()).isNull();
        String expectedExceptionMessageFormat = "%s during "
                + MaqSentimentRequestBody.class.getSimpleName()
                + " serialization: %s.";
        String expectedExceptionMessage =
                String.format(expectedExceptionMessageFormat,
                        stubException.getClass().getSimpleName(),
                        maqSentimentRequestBody);
        assertThat(maqSentimentResponse.getMessage())
                .isEqualTo(expectedExceptionMessage);
        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(listAppender.list.get(0).getMessage())
                .isEqualTo(String.format(expectedExceptionMessageFormat, "{}", "{}"));
        assertThat(listAppender.list.get(0).getFormattedMessage())
                .isEqualTo(expectedExceptionMessage);
        assertThat(listAppender.list.get(0).getArgumentArray()).isEqualTo(new Object[]{
                stubException.getClass().getSimpleName(),
                maqSentimentRequestBody});
        assertThat(((ThrowableProxy) listAppender.list.get(0).getThrowableProxy()).getThrowable()).isEqualTo(stubException);
        assertThat(listAppender.list.get(0).getMarker().getName()).isEqualTo("checked exception");
    }

    @Test
    void postSentiment_httpClient_send_throwsCheckedException() {
        InterruptedException stubException = new InterruptedException("stub exception message");
        assertDoesNotThrow(() -> when(httpClientMock.send(any(), any())).thenThrow(stubException));
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder().build();

        MaqSentimentResponse maqSentimentResponse = maqClientMock.postSentiment(maqSentimentRequestBody);

        assertThat(maqSentimentResponse.getUnderlyingResponse()).isNull();
        assertThat(maqSentimentResponse.getSuccesses()).isNull();
        assertThat(maqSentimentResponse.getErrors()).isNull();
        assertThat(maqSentimentResponse.getStatusCode()).isEqualTo(500);
        HttpRequest expectedHttpRequest = HttpRequest.newBuilder()
                .uri(URI_SENTIMENT)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(MAQ_API_KEY_NAME, maqKeyValueStub)
                .POST(HttpRequest.BodyPublishers.ofString("not displayed here"))
                .build();
        AtomicReference<String> expectedJsonMaqSentimentRequestBodyRef = new AtomicReference<>();
        assertDoesNotThrow(() -> expectedJsonMaqSentimentRequestBodyRef.set(
                objectMapper.writeValueAsString(maqSentimentRequestBody)));
        String expectedExceptionMessageFormat = "%s during HttpClient's HttpRequest. \r\n"
                + "HttpClient: \r\n%s\r\n" +
                "HttpRequest: \r\n%s\r\n" +
                "HttpRequest's Body: \r\n%s";
        String expectedExceptionMessage = String.format(expectedExceptionMessageFormat,
                stubException.getClass().getSimpleName(),
                httpClientMock,
                expectedHttpRequest,
                expectedJsonMaqSentimentRequestBodyRef.get());
        assertThat(maqSentimentResponse.getMessage())
                .isEqualTo(expectedExceptionMessage);
        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(listAppender.list.get(0).getMessage())
                .isEqualTo(String.format(expectedExceptionMessageFormat, "{}", "{}", "{}", "{}"));
        assertThat(listAppender.list.get(0).getFormattedMessage())
                .isEqualTo(expectedExceptionMessage);
        assertThat(listAppender.list.get(0).getArgumentArray())
                .isEqualTo(new Object[]{
                        stubException.getClass().getSimpleName(),
                        httpClientMock,
                        expectedHttpRequest,
                        expectedJsonMaqSentimentRequestBodyRef.get()});
        assertThat(((ThrowableProxy) listAppender.list.get(0).getThrowableProxy()).getThrowable()).isEqualTo(stubException);
        assertThat(listAppender.list.get(0).getMarker().getName()).isEqualTo("checked exception");
    }
}
