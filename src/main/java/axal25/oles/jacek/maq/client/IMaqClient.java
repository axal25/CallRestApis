package axal25.oles.jacek.maq.client;

import java.net.http.HttpResponse;

public interface IMaqClient {
    HttpResponse<String> postSentiment(String maqRequestBodyJson) throws MaqUnhandledException;
}
