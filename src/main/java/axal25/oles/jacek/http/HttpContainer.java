package axal25.oles.jacek.http;

import lombok.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class HttpContainer<T> {
    private HttpClient client;
    private HttpRequest request;
    private HttpResponse<T> response;
    private Throwable throwable;
    private String causeMessage;

    public static class HttpContainerBuilder<T> {
        @Getter
        private HttpClient client;
        @Getter
        private HttpRequest request;
        @Getter
        private HttpResponse<T> response;
        @Getter
        private Throwable throwable;
        @Getter
        private String causeMessage;
    }
}
