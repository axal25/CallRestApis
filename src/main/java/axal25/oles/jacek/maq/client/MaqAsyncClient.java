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
import java.util.concurrent.Executor;

@Component
public class MaqAsyncClient {
    private final MaqClientCommons maqClientCommons;
    private final MaqOmniSerializer maqOmniSerializer;
    private final Executor executor;

    @Autowired
    MaqAsyncClient(
            MaqClientCommons maqClientCommons,
            MaqOmniSerializer maqOmniSerializer,
            Executor executor) {
        this.maqClientCommons = maqClientCommons;
        this.maqOmniSerializer = maqOmniSerializer;
        this.executor = executor;
    }

    public CompletableFuture<MaqSentimentResponse> postSentiment(MaqSentimentRequestBody maqSentimentRequestBody) {
        return CompletableFuture.completedFuture(maqSentimentRequestBody)
                .thenApplyAsync(
                        body -> {
                            try {
                                return maqOmniSerializer.serializeToJson(body);
                            } catch (JsonProcessingException e) {
                                return CompletableFuture.<String>failedFuture(e).join();
                            }
                        },
                        executor)
                .thenApplyAsync(
                        bodyJson -> postSentiment(bodyJson)
                                .thenApplyAsync(
                                        maqOmniSerializer::deserializeFromJson,
                                        executor)
                                .join(),
                        executor)
                .handleAsync(
                        (maqSentimentResponse, throwable) -> {
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
                        },
                        executor);
    }

    public CompletableFuture<HttpContainer<String>> postSentiment(String maqSentimentRequestBodyJson) {
        HttpContainer.HttpContainerBuilder<String> containerBuilder =
                maqClientCommons.getContainerBuilder(maqSentimentRequestBodyJson);
        return containerBuilder.getClient().sendAsync(
                        containerBuilder.getRequest(),
                        HttpResponse.BodyHandlers.ofString())
                .handleAsync(
                        (httpResponse, throwable) -> {
                            if (throwable != null) {
                                return maqClientCommons.getHttpContainerFailedRequest(
                                        maqSentimentRequestBodyJson,
                                        containerBuilder,
                                        throwable);
                            }
                            return containerBuilder
                                    .response(httpResponse)
                                    .build();
                        },
                        executor);
    }
}
