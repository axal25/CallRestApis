package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.http.HttpContainer;
import axal25.oles.jacek.http.TestHttpResponse;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBodyDataElement;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseErrorBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseErrorBodyErrorsElement;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseSuccessBodyElement;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSession;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static axal25.oles.jacek.constant.Constants.CONTENT_TYPE;
import static axal25.oles.jacek.maq.MaqConstants.MAQ_API_KEY_NAME;
import static axal25.oles.jacek.maq.client.MaqClientCommons.URI_SENTIMENT;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class MaqOmniSerializerTest {
    private static final String MAQ_KEY_VALUE = "MAQ_KEY_VALUE";
    private static final HttpClient STUB_HTTP_CLIENT = HttpClient.newHttpClient();

    private static final HttpRequest STUB_HTTP_REQUEST = HttpRequest.newBuilder()
            .uri(URI_SENTIMENT)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(MAQ_API_KEY_NAME, MAQ_KEY_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString("maqSentimentRequestBodyJson"))
            .build();
    private static final TestHttpResponse<String> STUB_HTTP_RESPONSE = TestHttpResponse.<String>builder()
            .previousHttpResponse(null)
            .sslSession(mock(SSLSession.class))
            .httpClientVersion(HttpClient.Version.HTTP_1_1)
            .uri(URI_SENTIMENT)
            .httpRequest(STUB_HTTP_REQUEST)
            .httpHeaders(HttpHeaders.of(
                    Map.of(
                            "cache-control", List.of("private"),
                            "content-length", List.of("234"),
                            "content-type", List.of("application/json"),
                            "date", List.of("Mon, 24 Jul 2023 09:59:29 GMT"),
                            "request-context", List.of("appId=cid-v1:65946bdd-f7c2-41e1-848f-b5118188d656")),
                    (k, v) -> true))
            .build();
    private final Logger logger = (Logger) LoggerFactory.getLogger(MaqOmniSerializer.class);
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private ObjectMapper objectMapper;
    private MaqOmniSerializer maqOmniSerializer;

    @BeforeEach
    void setUp() {
        logger.addAppender(listAppender);
        logger.setLevel(Level.ALL);
        listAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        listAppender.start();
        objectMapper = mock(ObjectMapper.class, withSettings()
                .useConstructor()
                .defaultAnswer(CALLS_REAL_METHODS));
        maqOmniSerializer = mock(MaqOmniSerializer.class, withSettings()
                .useConstructor(objectMapper)
                .defaultAnswer(CALLS_REAL_METHODS));
    }

    @AfterEach
    void tearDown() {
        logger.detachAndStopAllAppenders();
    }

    @Test
    void serializeToJson_null_isSuccessful() throws JsonProcessingException {
        MaqSentimentRequestBody aNull = null;

        assertThat(maqOmniSerializer.serializeToJson(aNull)).isEqualTo(
                "null");
    }

    @Test
    void serializeToJson_dataNull_isSuccessful() throws JsonProcessingException {
        MaqSentimentRequestBody dataNull = MaqSentimentRequestBody.builder().build();

        assertThat(maqOmniSerializer.serializeToJson(dataNull)).isEqualTo(
                "{" +
                        "\"data\":null" +
                        "}");
    }

    @Test
    void serializeToJson_dataEmpty_isSuccessful() throws JsonProcessingException {
        MaqSentimentRequestBody dataEmpty = MaqSentimentRequestBody.builder()
                .data(List.of())
                .build();

        assertThat(maqOmniSerializer.serializeToJson(dataEmpty)).isEqualTo(
                "{" +
                        "\"data\":[]" +
                        "}");
    }

    @Test
    void serializeToJson_full_isSuccessful() throws JsonProcessingException {
        MaqSentimentRequestBody full = MaqSentimentRequestBody.builder()
                .data(List.of(MaqSentimentRequestBodyDataElement.builder()
                        .id("id_value")
                        .text("text_value")
                        .build()))
                .build();

        assertThat(maqOmniSerializer.serializeToJson(full)).isEqualTo(
                "{" +
                        "\"data\":[" +
                        "{" +
                        "\"id\":\"id_value\"," +
                        "\"text\":\"text_value\"" +
                        "}" +
                        "]" +
                        "}");
    }

    @Test
    void serializeToJson_objectMapperWritevalueAsString_throwsCheckedException() {
        JsonProcessingException stubException = mock(JsonProcessingException.class, withSettings()
                .useConstructor("stub exception message")
                .defaultAnswer(CALLS_REAL_METHODS));
        assertDoesNotThrow(() -> when(maqOmniSerializer.serializeToJson(any())).thenThrow(stubException));

        JsonProcessingException actualException =
                assertThrows(JsonProcessingException.class, () ->
                        maqOmniSerializer.serializeToJson(null));

        assertThat(actualException).isEqualTo(stubException);
    }

    @Test
    void deserializeFromJson_null() {
        HttpContainer<String> aNull = null;

        NullPointerException actualException =
                assertThrows(NullPointerException.class, () ->
                        maqOmniSerializer.deserializeFromJson(aNull));

        assertThat(actualException).hasMessageThat().isEqualTo(
                HttpContainer.class.getSimpleName() + " argument cannot be null.");
        assertThat(actualException).hasCauseThat().isNull();
    }

    @Test
    void deserializeFromJson_empty_throwsIllegalArgumentException() {
        HttpContainer<String> empty = HttpContainer.<String>builder().build();

        IllegalArgumentException actualException =
                assertThrows(IllegalArgumentException.class, () ->
                        maqOmniSerializer.deserializeFromJson(empty));

        assertThat(actualException).hasMessageThat().isEqualTo(
                HttpContainer.class.getSimpleName()
                        + " argument's Cause Message and "
                        + Throwable.class.getSimpleName()
                        + " cannot be null or blank at the same time.\r\n"
                        + "Cause Message: "
                        + empty.getCauseMessage()
                        + ", "
                        + Throwable.class.getSimpleName()
                        + ": "
                        + empty.getThrowable()
                        + ".");
        assertThat(actualException).hasCauseThat().isNull();
    }

    @Test
    void deserializeFromJson_nullResponseThrowableCauseMessage_throwsIllegalArgumentException() {
        HttpContainer<String> empty = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(null)
                .throwable(null)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .build();

        IllegalArgumentException actualException =
                assertThrows(IllegalArgumentException.class, () ->
                        maqOmniSerializer.deserializeFromJson(empty));

        assertThat(actualException).hasMessageThat().isEqualTo(
                HttpContainer.class.getSimpleName()
                        + " argument's Cause Message and "
                        + Throwable.class.getSimpleName()
                        + " cannot be null or blank at the same time.\r\n"
                        + "Cause Message: "
                        + empty.getCauseMessage()
                        + ", "
                        + Throwable.class.getSimpleName()
                        + ": "
                        + empty.getThrowable()
                        + ".");
        assertThat(actualException).hasCauseThat().isNull();
    }

    @Test
    void deserializeFromJson_nullResponseThrowable_causeMessageBlank_throwsIllegalArgumentException() {
        String stubCauseMessageBlank = " ";
        HttpContainer<String> empty = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(stubCauseMessageBlank)
                .throwable(null)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .build();

        IllegalArgumentException actualException =
                assertThrows(IllegalArgumentException.class, () ->
                        maqOmniSerializer.deserializeFromJson(empty));

        assertThat(actualException).hasMessageThat().isEqualTo(
                HttpContainer.class.getSimpleName()
                        + " argument's Cause Message and "
                        + Throwable.class.getSimpleName()
                        + " cannot be null or blank at the same time.\r\n"
                        + "Cause Message: "
                        + empty.getCauseMessage()
                        + ", "
                        + Throwable.class.getSimpleName()
                        + ": "
                        + empty.getThrowable()
                        + ".");
        assertThat(actualException).hasCauseThat().isNull();
    }

    @Test
    void deserializeFromJson_causeMessageAndThrowableOnly() {
        String stubCauseMessage = "stub cause message";
        Throwable stubThrowable = new Throwable("stub throwable message");
        HttpContainer<String> throwableOnly = HttpContainer.<String>builder()
                .causeMessage(stubCauseMessage)
                .throwable(stubThrowable)
                .throwable(null)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .build();

        MaqSentimentResponse actualResponse = maqOmniSerializer.deserializeFromJson(throwableOnly);

        assertThat(actualResponse).isEqualTo(MaqSentimentResponse.builder()
                .errorBody(MaqSentimentResponseErrorBody.builder()
                        .statusCode(500)
                        .message(stubCauseMessage)
                        .build())
                .build());
    }

    @Test
    void deserializeFromJson_throwableOnly() {
        Throwable stubThrowable = new Throwable("stub throwable message",
                new Exception("stub nested exception message",
                        new RuntimeException(" ")));
        HttpContainer<String> throwableOnly = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(null)
                .throwable(stubThrowable)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .build();

        MaqSentimentResponse actualResponse = maqOmniSerializer.deserializeFromJson(throwableOnly);

        assertThat(actualResponse).isEqualTo(MaqSentimentResponse.builder()
                .errorBody(MaqSentimentResponseErrorBody.builder()
                        .statusCode(500)
                        .message(stubThrowable.getCause().getMessage())
                        .build())
                .build());
    }

    @Test
    void deserializeFromJson_throwableEmptyOnly() {
        Throwable stubThrowableEmpty = new Throwable();
        HttpContainer<String> throwableOnly = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(null)
                .throwable(stubThrowableEmpty)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .build();

        MaqSentimentResponse actualResponse = maqOmniSerializer.deserializeFromJson(throwableOnly);

        assertThat(actualResponse).isEqualTo(MaqSentimentResponse.builder()
                .errorBody(MaqSentimentResponseErrorBody.builder()
                        .statusCode(500)
                        .message(stubThrowableEmpty.getClass().getSimpleName())
                        .build())
                .build());
    }

    @Test
    void deserializeFromJson_withResponse_statusCode400_bodyJsonObjectErrorsArray() {
        HttpResponse<String> httpResponse = STUB_HTTP_RESPONSE.toBuilder()
                .statusCode(400)
                .body("{\n" +
                        "  \"errors\": [\n" +
                        "    {\n" +
                        "      \"property\": \"text\",\n" +
                        "      \"recordNumber\": 1,\n" +
                        "      \"validator\": \"Empty string check\",\n" +
                        "      \"value\": \"null\",\n" +
                        "      \"message\": \"InvalidJSONError: The ‘text’ passed in json is empty\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .build();
        HttpContainer<String> withResponse = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(null)
                .throwable(null)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .response(httpResponse)
                .build();

        MaqSentimentResponse actualResponse = maqOmniSerializer.deserializeFromJson(withResponse);

        assertThat(actualResponse).isEqualTo(MaqSentimentResponse.builder()
                .underlyingResponse(httpResponse)
                .successBody(null)
                .errorBody(MaqSentimentResponseErrorBody.builder()
                        .statusCode(null)
                        .message(null)
                        .errors(List.of(
                                MaqSentimentResponseErrorBodyErrorsElement.builder()
                                        .property("text")
                                        .recordNumber(1L)
                                        .validator("Empty string check")
                                        .value("null")
                                        .message("InvalidJSONError: The ‘text’ passed in json is empty")
                                        .build()))
                        .build())
                .build());
    }

    @Test
    void deserializeFromJson_withResponse_statusCode400_bodyStringNotJsonObject() {
        HttpResponse<String> httpResponse = STUB_HTTP_RESPONSE.toBuilder()
                .statusCode(400)
                .body("Object reference not set to an instance of an object.")
                .build();
        HttpContainer<String> withResponse = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(null)
                .throwable(null)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .response(httpResponse)
                .build();

        MaqSentimentResponse actualResponse = maqOmniSerializer.deserializeFromJson(withResponse);

        assertThat(actualResponse).isEqualTo(MaqSentimentResponse.builder()
                .underlyingResponse(httpResponse)
                .successBody(null)
                .errorBody(MaqSentimentResponseErrorBody.builder()
                        .statusCode(400)
                        .message("Object reference not set to an instance of an object.")
                        .errors(null)
                        .build())
                .build());
    }

    @Test
    void deserializeFromJson_withResponse_statusCode400_bodyJsonObjectStatusCodeAndMessage() {
        HttpResponse<String> httpResponse = STUB_HTTP_RESPONSE.toBuilder()
                .statusCode(401)
                .body("{ \"statusCode\": 401, \"message\": \"You are passing an invalid API Key. Please valdiate the API Key. For further assistance, get in touch with us here:  https://maqsoftware.com/contact\" }")
                .build();
        HttpContainer<String> withResponse = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(null)
                .throwable(null)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .response(httpResponse)
                .build();

        MaqSentimentResponse actualResponse = maqOmniSerializer.deserializeFromJson(withResponse);

        assertThat(actualResponse).isEqualTo(MaqSentimentResponse.builder()
                .underlyingResponse(httpResponse)
                .successBody(null)
                .errorBody(MaqSentimentResponseErrorBody.builder()
                        .statusCode(401)
                        .message("You are passing an invalid API Key. Please valdiate the API Key. For further assistance, get in touch with us here:  https://maqsoftware.com/contact")
                        .errors(null)
                        .build())
                .build());
    }

    @Test
    void deserializeFromJson_withResponse_statusCode400_bodyJson_objectMapperReadValue_throwsCheckedException() {
        HttpResponse<String> httpResponse = STUB_HTTP_RESPONSE.toBuilder()
                .statusCode(401)
                .body("{httpResponse_body_value}")
                .build();
        HttpContainer<String> withResponse = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(null)
                .throwable(null)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .response(httpResponse)
                .build();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        objectMapper = mock(ObjectMapper.class);
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        maqOmniSerializer = mock(MaqOmniSerializer.class, withSettings()
                .useConstructor(objectMapper)
                .defaultAnswer(CALLS_REAL_METHODS));
        JsonProcessingException stubException = mock(JsonProcessingException.class, withSettings()
                .useConstructor("stub exception message")
                .defaultAnswer(CALLS_REAL_METHODS));
        assertDoesNotThrow(() ->
                when(objectMapper.readValue(anyString(), any(Class.class))).thenThrow(stubException));

        MaqSentimentResponse actualResponse = maqOmniSerializer.deserializeFromJson(withResponse);

        String expectedExceptionMessageFormat = "%s during deserialization of "
                + MaqSentimentResponseErrorBody.class.getSimpleName()
                + " from "
                + HttpResponse.class.getSimpleName()
                + "'s Body:\r\n"
                + "%s";
        String expectedExceptionMessage = String.format(expectedExceptionMessageFormat,
                stubException.getClass().getSimpleName(),
                httpResponse.body());
        assertThat(actualResponse).isEqualTo(MaqSentimentResponse.builder()
                .underlyingResponse(httpResponse)
                .successBody(null)
                .errorBody(MaqSentimentResponseErrorBody.builder()
                        .statusCode(httpResponse.statusCode())
                        .message(expectedExceptionMessage)
                        .errors(null)
                        .build())
                .build());
        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(listAppender.list.get(0).getMessage())
                .isEqualTo(String.format(expectedExceptionMessageFormat, "{}", "{}"));
        assertThat(listAppender.list.get(0).getFormattedMessage())
                .isEqualTo(expectedExceptionMessage);
        assertThat(listAppender.list.get(0).getArgumentArray()).isEqualTo(new Object[]{
                stubException.getClass().getSimpleName(),
                httpResponse.body()});
        assertThat(((ThrowableProxy) listAppender.list.get(0).getThrowableProxy()).getThrowable()).isEqualTo(stubException);
        assertThat(listAppender.list.get(0).getMarker().getName()).isEqualTo("checked exception");
    }

    @Test
    void deserializeFromJson_withResponse_statusCode200_bodyJsonArrayOfSentiments() {
        HttpResponse<String> httpResponse = STUB_HTTP_RESPONSE.toBuilder()
                .statusCode(200)
                .body("[\n" +
                        "  {\n" +
                        "    \"id\": \"1\",\n" +
                        "    \"sentiment\": 0.9533200264\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"id\": \"2\",\n" +
                        "    \"sentiment\": 0.0114474036\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"id\": \"3\",\n" +
                        "    \"sentiment\": 0.9497712851\n" +
                        "  }\n" +
                        "]")
                .build();
        HttpContainer<String> withResponse = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(null)
                .throwable(null)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .response(httpResponse)
                .build();

        MaqSentimentResponse actualResponse = maqOmniSerializer.deserializeFromJson(withResponse);

        assertThat(actualResponse).isEqualTo(MaqSentimentResponse.builder()
                .underlyingResponse(httpResponse)
                .successBody(List.of(
                        MaqSentimentResponseSuccessBodyElement.builder()
                                .id("1")
                                .sentiment(new BigDecimal("0.9533200264"))
                                .build(),
                        MaqSentimentResponseSuccessBodyElement.builder()
                                .id("2")
                                .sentiment(new BigDecimal("0.0114474036"))
                                .build(),
                        MaqSentimentResponseSuccessBodyElement.builder()
                                .id("3")
                                .sentiment(new BigDecimal("0.9497712851"))
                                .build()))
                .errorBody(null)
                .build());
    }

    @Test
    void deserializeFromJson_withResponse_statusCode200_objectMapperReadValue_throwsCheckedException() {
        HttpResponse<String> httpResponse = STUB_HTTP_RESPONSE.toBuilder()
                .statusCode(200)
                .body("httpResponse_body_value")
                .build();
        HttpContainer<String> withResponse = HttpContainer.<String>builder()
                .response(null)
                .causeMessage(null)
                .throwable(null)
                .client(STUB_HTTP_CLIENT)
                .request(STUB_HTTP_REQUEST)
                .response(httpResponse)
                .build();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        objectMapper = mock(ObjectMapper.class);
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        maqOmniSerializer = mock(MaqOmniSerializer.class, withSettings()
                .useConstructor(objectMapper)
                .defaultAnswer(CALLS_REAL_METHODS));
        JsonProcessingException stubException = mock(JsonProcessingException.class, withSettings()
                .useConstructor("stub exception message")
                .defaultAnswer(CALLS_REAL_METHODS));
        assertDoesNotThrow(() ->
                when(objectMapper.readValue(anyString(), any(JavaType.class))).thenThrow(stubException));

        MaqSentimentResponse actualResponse = maqOmniSerializer.deserializeFromJson(withResponse);

        String expectedExceptionMessageFormat = "%s during deserialization of "
                + List.class.getSimpleName()
                + "<"
                + MaqSentimentResponseSuccessBodyElement.class
                + "> from "
                + HttpResponse.class.getSimpleName()
                + "'s Body:\r\n" +
                "%s";
        String expectedExceptionMessage = String.format(expectedExceptionMessageFormat,
                stubException.getClass().getSimpleName(),
                httpResponse.body());
        assertThat(actualResponse).isEqualTo(MaqSentimentResponse.builder()
                .underlyingResponse(httpResponse)
                .successBody(null)
                .errorBody(MaqSentimentResponseErrorBody.builder()
                        .statusCode(500)
                        .message(expectedExceptionMessage)
                        .errors(null)
                        .build())
                .build());
        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(listAppender.list.get(0).getMessage())
                .isEqualTo(String.format(expectedExceptionMessageFormat, "{}", "{}"));
        assertThat(listAppender.list.get(0).getFormattedMessage())
                .isEqualTo(expectedExceptionMessage);
        assertThat(listAppender.list.get(0).getArgumentArray()).isEqualTo(new Object[]{
                stubException.getClass().getSimpleName(),
                httpResponse.body()});
        assertThat(((ThrowableProxy) listAppender.list.get(0).getThrowableProxy()).getThrowable()).isEqualTo(stubException);
        assertThat(listAppender.list.get(0).getMarker().getName()).isEqualTo("checked exception");
    }
}
