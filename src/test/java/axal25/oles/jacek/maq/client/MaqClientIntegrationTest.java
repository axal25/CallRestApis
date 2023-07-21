package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBodyDataElement;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponseErrorBodyErrorsElement;
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
    private MaqClient maqClient;
    @Autowired
    private MaqOmniSerializer maqOmniSerializer;

    @Test
    void postSentiment_successful_200() {
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
        assertThat(maqSentimentResponse.getSuccessBody()).isNotNull();
        assertThat(maqSentimentResponse.getSuccessBody()).hasSize(maqSentimentRequestBody.getData().size());
        IntStream.range(0, maqSentimentResponse.getSuccessBody().size()).forEach(i -> {
            assertThat(maqSentimentResponse.getSuccessBody().get(i)).isNotNull();
            assertThat(maqSentimentResponse.getSuccessBody().get(i).getId())
                    .isEqualTo(maqSentimentRequestBody.getData().get(i).getId());
            assertThat(maqSentimentResponse.getSuccessBody().get(i).getSentiment()).isNotNull();
        });
        assertThat(maqSentimentResponse.getErrorBody()).isNull();
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
        MaqClient maqClient = new MaqClient("bad maq api key value", maqOmniSerializer);

        MaqSentimentResponse maqSentimentResponse = maqClient.postSentiment(maqSentimentRequestBody);

        assertThat(maqSentimentResponse.getUnderlyingResponse().statusCode()).isEqualTo(401);
        assertThat(maqSentimentResponse.getSuccessBody()).isNull();
        assertThat(maqSentimentResponse.getErrorBody()).isNotNull();
        assertThat(maqSentimentResponse.getErrorBody().getStatusCode()).isEqualTo(401);
        assertThat(maqSentimentResponse.getErrorBody().getMessage())
                .isEqualTo("You are passing an invalid API Key. " +
                        "Please valdiate the API Key. For further assistance, get in touch with us here:  " +
                        "https://maqsoftware.com/contact");
        assertThat(maqSentimentResponse.getErrorBody().getErrors()).isNull();
    }

    @Test
    void postSentiment_emptyBody_400() {
        MaqSentimentRequestBody maqSentimentRequestBody = MaqSentimentRequestBody.builder().build();

        MaqSentimentResponse maqSentimentResponse = maqClient.postSentiment(maqSentimentRequestBody);

        assertThat(maqSentimentResponse.getUnderlyingResponse().statusCode()).isEqualTo(400);
        assertThat(maqSentimentResponse.getSuccessBody()).isNull();
        assertThat(maqSentimentResponse.getErrorBody()).isNotNull();
        assertThat(maqSentimentResponse.getErrorBody().getStatusCode()).isEqualTo(400);
        assertThat(maqSentimentResponse.getErrorBody().getMessage()).isEqualTo("Object reference not set to an instance of an object.");
        assertThat(maqSentimentResponse.getErrorBody().getErrors()).isNull();
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
        assertThat(maqSentimentResponse.getSuccessBody()).isNull();
        assertThat(maqSentimentResponse.getErrorBody()).isNotNull();
        assertThat(maqSentimentResponse.getErrorBody().getStatusCode()).isNull();
        assertThat(maqSentimentResponse.getErrorBody().getMessage()).isNull();
        assertThat(maqSentimentResponse.getErrorBody().getErrors()).isEqualTo(
                maqSentimentRequestBody.getData().stream().map(request ->
                                MaqSentimentResponseErrorBodyErrorsElement.builder()
                                        .property("text")
                                        .recordNumber(Long.valueOf(request.getId()))
                                        .validator("Empty string check")
                                        .value("null")
                                        .message("InvalidJSONError: The ‘text’ passed in json is empty")
                                        .build())
                        .collect(toList()));
    }
}
