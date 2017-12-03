package hello;


import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URI;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 3..
 */
public class HttpConnection {
    private final static HttpClient httpClient = HttpClientBuilder.create().build();
    private final static String ACCESS_TOKEN = "9hPQz4rrGETJtpFpfIrVxg4RY5jwezlrIT6l4TCCwNu7VBCqXan2Iwvq1AOEjJgPal3nZQdH/zmEr7bYeNkz3QcmTn25abpX234CjqH+9jusUvFnHHCbE8mIy3e6u4WUJ/wzssuxOYD7Ut3Pd+OwNwdB04t89/1O/w1cDnyilFU=";

    public boolean post(String uriStr, String payload) {

        try {
            System.out.println("payload : " + payload);

            URI uri = new URI(uriStr);
            HttpPost postRequest = new HttpPost(uri);

            String token = "Bearer " + ACCESS_TOKEN;
            StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
            postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            postRequest.addHeader("Authorization", token);
            postRequest.setEntity(entity);

            System.out.println("Token : " + token);
            System.out.println("postRequest : " + postRequest.toString());
            System.out.println("Header : " + postRequest.getAllHeaders());
            System.out.println("Entity : " + postRequest.getEntity());

            HttpResponse httpResponse = httpClient.execute(postRequest);
            System.out.println("LINE Platform Server Response : " + httpResponse);
        } catch(Exception e) {
            return false;
        }

        return true;
    }
}
