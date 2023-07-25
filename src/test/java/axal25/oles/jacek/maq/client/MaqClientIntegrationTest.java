package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.http.HttpContainer;
import axal25.oles.jacek.maq.model.MaqOmniSerializer;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBodyDataElement;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseErrorBodyElement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;
import static java.util.stream.Collectors.toList;

@SpringBootTest
public class MaqClientIntegrationTest {
    @Autowired
    private MaqOmniSerializer maqOmniSerializer;
    @Autowired
    private MaqClient maqClient;

    @Test
    void postSentiment_failure_400_singleObject() {
        String bodyJson = "{ \"id\": \"1\", \"text\": \"I love working on ML stuff.\" }";

        HttpContainer<String> httpContainer = maqClient.postSentiment(bodyJson);

        MaqSentimentResponse maqSentimentResponse = maqOmniSerializer.deserializeFromJson(httpContainer);
        assertThat(maqSentimentResponse.getStatusCode()).isEqualTo(400);
        assertThat(maqSentimentResponse.getUnderlyingResponse()).isNotNull();
        assertThat(maqSentimentResponse.getSuccesses()).isNull();
        assertThat(maqSentimentResponse.getMessage()).isNull();
        assertThat(maqSentimentResponse.getErrors()).isEqualTo(List.of(
                MaqSentimentResponseErrorBodyElement.builder()
                        .property("data")
                        .validator("Not applicable")
                        .value("null")
                        .message("InvalidJSONError: Not a valid format expected ‘data’ as a key in input json. Please send json in template as such {data : [{id: “numerical id here” , text: “text here” }…….{id: “numerical id here” , text: “text here” }]}")
                        .build()));
    }

    @Test
    void postSentiment_failure_400_singleObjectInDataObject() {
        String bodyJson = "{ \"data\": " +
                "{ \"id\": \"1\", \"text\": \"I love working on ML stuff.\" }" +
                " }";

        HttpContainer<String> httpContainer = maqClient.postSentiment(bodyJson);

        assertThat(httpContainer.getResponse().body())
                .contains("Cannot deserialize the current JSON object (e.g. {\"name\":\"value\"}) into type 'System.Collections.Generic.List`1[Newtonsoft.Json.Linq.JObject]' because the type requires a JSON array (e.g. [1,2,3])");
        assertThat(httpContainer.getResponse().statusCode()).isEqualTo(400);
    }

    @Test
    void postSentiment_successful_200_singleObjectInDataArray() {
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder()
                .data(List.of(
                        MaqSentimentRequestBodyDataElement.builder()
                                .id("1")
                                .text("I love working on ML stuff.")
                                .build()))
                .build();

        MaqSentimentResponse maqSentimentResponse = maqClient.postSentiment(maqSentimentRequestBody);

        assertThat(maqSentimentResponse.getUnderlyingResponse().statusCode()).isEqualTo(200);
        assertThat(maqSentimentResponse.getSuccesses()).isNotNull();
        assertThat(maqSentimentResponse.getSuccesses()).hasSize(maqSentimentRequestBody.getData().size());
        IntStream.range(0, maqSentimentResponse.getSuccesses().size()).forEach(i -> {
            assertThat(maqSentimentResponse.getSuccesses().get(i)).isNotNull();
            assertThat(maqSentimentResponse.getSuccesses().get(i).getId())
                    .isEqualTo(maqSentimentRequestBody.getData().get(i).getId());
            assertThat(maqSentimentResponse.getSuccesses().get(i).getSentiment()).isNotNull();
        });
        assertThat(maqSentimentResponse.getErrors()).isNull();
    }

    @Test
    void postSentiment_successful_200_multipleObjectsInDataArray() {
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder()
                .data(List.of(
                        MaqSentimentRequestBodyDataElement.builder()
                                .id("1")
                                .text("I love working on ML stuff.")
                                .build(),
                        MaqSentimentRequestBodyDataElement.builder()
                                .id("2")
                                .text("I hate working on stuff that is boring and repetitive.")
                                .build(),
                        MaqSentimentRequestBodyDataElement.builder()
                                .id("3")
                                .text("I love working from home in COVID-19.")
                                .build()))
                .build();

        MaqSentimentResponse maqSentimentResponse = maqClient.postSentiment(maqSentimentRequestBody);

        assertThat(maqSentimentResponse.getUnderlyingResponse().statusCode()).isEqualTo(200);
        assertThat(maqSentimentResponse.getSuccesses()).isNotNull();
        assertThat(maqSentimentResponse.getSuccesses()).hasSize(maqSentimentRequestBody.getData().size());
        IntStream.range(0, maqSentimentResponse.getSuccesses().size()).forEach(i -> {
            assertThat(maqSentimentResponse.getSuccesses().get(i)).isNotNull();
            assertThat(maqSentimentResponse.getSuccesses().get(i).getId())
                    .isEqualTo(maqSentimentRequestBody.getData().get(i).getId());
            assertThat(maqSentimentResponse.getSuccesses().get(i).getSentiment()).isNotNull();
        });
        assertThat(maqSentimentResponse.getErrors()).isNull();
    }

    @Test
    void postSentiment_badMaqApiKeyValue_401() {
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder()
                .data(List.of(
                        MaqSentimentRequestBodyDataElement.builder()
                                .id("0")
                                .text("test")
                                .build()))
                .build();
        MaqClientCommons maqClientCommons = new MaqClientCommons("bad maq api key value");
        MaqClient maqClient = new MaqClient(maqClientCommons, maqOmniSerializer);

        MaqSentimentResponse maqSentimentResponse = maqClient.postSentiment(maqSentimentRequestBody);

        assertThat(maqSentimentResponse.getUnderlyingResponse().statusCode()).isEqualTo(401);
        assertThat(maqSentimentResponse.getSuccesses()).isNull();
        assertThat(maqSentimentResponse.getStatusCode()).isEqualTo(401);
        assertThat(maqSentimentResponse.getMessage())
                .isEqualTo("You are passing an invalid API Key. " +
                        "Please valdiate the API Key. For further assistance, get in touch with us here:  " +
                        "https://maqsoftware.com/contact");
        assertThat(maqSentimentResponse.getErrors()).isNull();
    }

    @Test
    void postSentiment_emptyBody_400() {
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder().build();

        MaqSentimentResponse maqSentimentResponse = maqClient.postSentiment(maqSentimentRequestBody);

        assertThat(maqSentimentResponse.getUnderlyingResponse().statusCode()).isEqualTo(400);
        assertThat(maqSentimentResponse.getSuccesses()).isNull();
        assertThat(maqSentimentResponse.getStatusCode()).isEqualTo(400);
        assertThat(maqSentimentResponse.getMessage()).isEqualTo("Object reference not set to an instance of an object.");
        assertThat(maqSentimentResponse.getErrors()).isNull();
    }

    @Test
    void postSentiment_emptyString_400() {
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder()
                .data(List.of(
                        MaqSentimentRequestBodyDataElement.builder()
                                .id("1")
                                .text("")
                                .build()))
                .build();

        MaqSentimentResponse maqSentimentResponse = maqClient.postSentiment(maqSentimentRequestBody);

        assertThat(maqSentimentResponse.getUnderlyingResponse().statusCode()).isEqualTo(400);
        assertThat(maqSentimentResponse.getSuccesses()).isNull();
        assertThat(maqSentimentResponse.getStatusCode()).isEqualTo(400);
        assertThat(maqSentimentResponse.getMessage()).isNull();
        assertThat(maqSentimentResponse.getErrors()).isEqualTo(
                maqSentimentRequestBody.getData().stream().map(request ->
                                MaqSentimentResponseErrorBodyElement.builder()
                                        .property("text")
                                        .recordNumber(Long.valueOf(request.getId()))
                                        .validator("Empty string check")
                                        .value("null")
                                        .message("InvalidJSONError: The ‘text’ passed in json is empty")
                                        .build())
                        .collect(toList()));
    }
}
