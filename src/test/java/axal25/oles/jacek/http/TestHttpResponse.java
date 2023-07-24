package axal25.oles.jacek.http;

import lombok.*;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TestHttpResponse<T> implements HttpResponse<T> {
    private int statusCode;
    private HttpRequest httpRequest;
    private HttpResponse<T> previousHttpResponse;
    private HttpHeaders httpHeaders;
    private T body;
    private SSLSession sslSession;
    private URI uri;
    private HttpClient.Version httpClientVersion;

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public HttpRequest request() {
        return httpRequest;
    }

    @Override
    public Optional<HttpResponse<T>> previousResponse() {
        return Optional.ofNullable(previousHttpResponse);
    }

    @Override
    public HttpHeaders headers() {
        return httpHeaders;
    }

    @Override
    public T body() {
        return body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return Optional.ofNullable(sslSession);
    }

    @Override
    public URI uri() {
        return uri;
    }

    @Override
    public HttpClient.Version version() {
        return httpClientVersion;
    }
}
