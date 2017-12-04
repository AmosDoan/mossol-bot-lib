package hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Amos.Doan.Mac on 2017. 11. 18..
 */
@RestController
public class MossolController {
    private static final String template = "Hello, %s!";
    private static final String SECRET_KEY = "49588e53b2c64f47a3fc84739e17b757";
    private static final String REPLY_URI = "https://api.line.me/v2/bot/message/reply";
    private static final HttpConnection httpConnection = new HttpConnection();

    private final AtomicLong counter = new AtomicLong();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LineRequest readJsonString(String jsonString) {
        try {
            LineRequest request;
            request = OBJECT_MAPPER.readValue(jsonString, LineRequest.class);
            return request;
       } catch (Exception e) {
            System.out.println("[ERROR] Converting to object failed");
            return null;
        }
    }

    private String writeJsonString(Object obj) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean validateHeader(String requestBody) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(requestBody.getBytes()));
            System.out.println("Hash Result :" + hash);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean replyMessage(LineRequest request) {
       LineRequest.Event event =  request.getEvents().get(0);
       String token = event.getReplyToken();

        System.out.println("DEBUG : " + event.getMessage().getText() );

       if (event.getMessage().getText().equals("안녕?")) {
           System.out.println("====send reply====");
           LineReplyRequest replyRequest = new LineReplyRequest();
           replyRequest.setReplyToken(token);

           LineReplyRequest.Message message = new LineReplyRequest.Message();
           message.setText("Hi from Mossol!!!");
           message.setType("text");
           replyRequest.setMessage(message);

           if(httpConnection.post(REPLY_URI, writeJsonString(replyRequest))) {
               return true;
           } else {
               return false;
           }
       }

        System.out.println("====No reply====");
       return false;
    }

    @RequestMapping("/greeting")
    public HelloGreeting greeting(@RequestParam(value = "name", defaultValue = "world")String name) {
        System.out.println("greeting!");
        return new HelloGreeting(counter.incrementAndGet(), String.format(template, name));
    }

    @RequestMapping(value = "/line", method = RequestMethod.POST)
    public LineResponse getLine(@RequestHeader(value = "X-Line-Signature") String signature,
                                @RequestBody String request) {
        LineRequest requestObj = readJsonString(request);
        LineResponse response = new LineResponse();

        if(!validateHeader(request)) {
            System.out.println("ERROR : Abusing API Call!");
            return null;
        }

        response.setResponse(requestObj.getEvents().toString());

        System.out.println("header signature : " + signature);
        System.out.println("SECRET: " + SECRET_KEY);
        System.out.println("response : " + request);

        replyMessage(requestObj);

        return response;
    }
}
