package axal25.oles.jacek.maq.client;

import axal25.oles.jacek.http.HttpContainer;
import axal25.oles.jacek.maq.model.MaqOmniSerializer;
import axal25.oles.jacek.maq.model.request.MaqSentimentRequestBody;
import axal25.oles.jacek.maq.model.response.MaqSentimentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
public class MaqAsyncClient {
    private final MaqClientCommons maqClientCommons;
    private final MaqOmniSerializer maqOmniSerializer;

    @Autowired
    public MaqAsyncClient(
            MaqClientCommons maqClientCommons,
            MaqOmniSerializer maqOmniSerializer) {
        this.maqClientCommons = maqClientCommons;
        this.maqOmniSerializer = maqOmniSerializer;
    }

    public CompletableFuture<MaqSentimentResponse> postSentiment(MaqSentimentRequestBody maqSentimentRequestBody) {
        // just to show-off
        return CompletableFuture.completedFuture(maqSentimentRequestBody)
                .thenApplyAsync(body -> {
                    try {
                        return maqOmniSerializer.serializeToJson(body);
                    } catch (JsonProcessingException e) {
                        return CompletableFuture.<String>failedFuture(e).join();
                    }
                })
                .thenApplyAsync(bodyJson -> postSentiment(bodyJson)
                        .thenApplyAsync(maqOmniSerializer::deserializeFromJson)
                        .join())
                .handleAsync((maqSentimentResponse, throwable) -> {
                    if (throwable != null) {
                        Throwable underlyingCause =
                                throwable instanceof CompletionException
                                        && throwable.getCause() != null
                                        ? throwable.getCause()
                                        : throwable;
                        return maqOmniSerializer.deserializeFromJson(
                                maqClientCommons.getHttpContainerFailedSerialization(
                                        maqSentimentRequestBody,
                                        underlyingCause));
                    }
                    return maqSentimentResponse;
                });

        // equivalent to
        // String maqSentimentRequestBodyJson = null;
        // try {
        //    maqSentimentRequestBodyJson = maqOmniSerializer.serializeToJson(maqSentimentRequestBody);
        // } catch (JsonProcessingException e) {
        //    return CompletableFuture.completedFuture(
        //            maqOmniSerializer.deserializeFromJson(
        //                    maqClientCommons.getHttpContainerFailedSerialization(
        //                            maqSentimentRequestBody,
        //                            e)));
        // }
        // return postSentiment(maqSentimentRequestBodyJson)
        //        .thenApplyAsync(maqOmniSerializer::deserializeFromJson);
    }

    public CompletableFuture<HttpContainer<String>> postSentiment(String maqSentimentRequestBodyJson) {
        HttpContainer.HttpContainerBuilder<String> containerBuilder =
                maqClientCommons.getContainerBuilder(maqSentimentRequestBodyJson);
        return containerBuilder.getClient().sendAsync(
                        containerBuilder.getRequest(),
                        HttpResponse.BodyHandlers.ofString())
                .handleAsync((httpResponse, throwable) -> {
                    if (throwable != null) {
                        return maqClientCommons.getHttpContainerFailedRequest(
                                maqSentimentRequestBodyJson,
                                containerBuilder,
                                throwable);
                    }
                    return containerBuilder
                            .response(httpResponse)
                            .build();
                });
    }
}
