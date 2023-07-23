package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.http.HttpContainer;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpResponse;

@Component
public class MaqClient {
    private final MaqClientCommons maqClientCommons;
    private final MaqOmniSerializer maqOmniSerializer;

    @Autowired
    public MaqClient(
            MaqClientCommons maqClientCommons,
            MaqOmniSerializer maqOmniSerializer) {
        this.maqClientCommons = maqClientCommons;
        this.maqOmniSerializer = maqOmniSerializer;
    }

    public MaqSentimentResponse postSentiment(MaqSentimentRequestBody maqSentimentRequestBody) {
        String maqSentimentRequestBodyJson = null;

        try {
            maqSentimentRequestBodyJson = maqOmniSerializer.serializeToJson(maqSentimentRequestBody);
        } catch (JsonProcessingException e) {
            return maqOmniSerializer.deserializeFromJson(
                    maqClientCommons.getHttpContainerFailedSerialization(
                            maqSentimentRequestBody,
                            e));
        }

        return maqOmniSerializer.deserializeFromJson(
                postSentiment(maqSentimentRequestBodyJson));
    }

    public HttpContainer<String> postSentiment(String maqSentimentRequestBodyJson) {
        HttpContainer.HttpContainerBuilder<String> containerBuilder =
                maqClientCommons.getContainerBuilder(maqSentimentRequestBodyJson);

        HttpResponse<String> httpResponse;
        try {
            httpResponse = containerBuilder.getClient().send(
                    containerBuilder.getRequest(),
                    HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return maqClientCommons.getHttpContainerFailedRequest(
                    maqSentimentRequestBodyJson,
                    containerBuilder,
                    e);
        }

        return containerBuilder
                .response(httpResponse)
                .build();
    }
}
