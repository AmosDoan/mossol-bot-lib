package net.mossol;

import com.fasterxml.jackson.databind.JsonNode;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.annotation.*;
import net.mossol.connection.RetrofitConnection;
import net.mossol.model.*;
import net.mossol.service.MessageHandler;
import net.mossol.util.MessageBuildUtil;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Amos.Doan.Mac on 2017. 11. 18..
 */
@Service
public class MossolLineController {
    private static final Logger logger = LoggerFactory.getLogger(MossolLineController.class);
    private static final String template = "%dth, Hello, %s!";

    @Value("${line.secret}")
    private String SECRET_KEY;

    private final AtomicLong counter = new AtomicLong();

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private RetrofitConnection retrofitConnection;

    private boolean sendFoodRequest(String token, MenuInfo menu) {
        return sendRequest(MessageBuildUtil.sendFoodMessage(token, menu));
    }

    private boolean sendRequest(LineReplyRequest request) {
        String payload = MossolUtil.writeJsonString(request);
        logger.debug("sendRequest Payload : {}", payload);
        retrofitConnection.sendReply(request);
        return true;
    }

    private boolean leaveRoom(String groupId) {
        retrofitConnection.leaveRoom(null, groupId);
        return true;
    }

    private boolean validateHeader(String requestBody, String signature) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(requestBody.getBytes()));
            if (signature.equals(hash)) {
                logger.debug("PASS: Hash Result {}, Signature {}", hash, signature);
                return true;
            } else {
                logger.debug("FAIL: Hash Result {}, Signature {}", hash, signature);
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void handleMessage(LineRequest.Event event) throws Exception {
        final String token = event.getReplyToken();
        final String message = event.getMessage().getText();

        ReplyMessage replyMessage = messageHandler.replyMessage(message);
        if (replyMessage == null) {
            logger.debug("INFO: there is no matching reply message");
            return;
        }

        TextType type = replyMessage.getType();

        switch(type) {
            case SHOW_MENU_K:
            case SHOW_MENU_J:
            case SHOW_MENU_D:
            case ADD_MENU_K:
            case ADD_MENU_J:
            case ADD_MENU_D:
            case DEL_MENU_K:
            case DEL_MENU_J:
            case DEL_MENU_D:
            case TEXT:
                sendRequest(MessageBuildUtil.sendTextMessage(token, replyMessage.getText()));
                return;
            case SELECT_MENU_K:
            case SELECT_MENU_J:
            case SELECT_MENU_D:
                sendFoodRequest(token, replyMessage.getMenuInfo());
                return;
            case LEAVE_ROOM:
                String groupId =  event.getSource().getGroupId();
                leaveRoom(groupId);
                break;
        }

        throw new Exception("Send message failed");
    }

    @Get("/healthCheck")
    public HttpResponse healthCheck(@Param("name") @Default("world") String name) {
        logger.debug("health check");
        return HttpResponse.of(HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8,
                               String.format(template, counter.incrementAndGet(), name));
    }

    @Post
    @Path("/line")
    public HttpResponse getLine(@Header("X-Line-Signature") String signature,
                                @RequestObject JsonNode request) {
        LineRequest requestObj = MossolUtil.readJsonString(request);

        if (requestObj == null) {
            return null;
        }

        if(!validateHeader(request.toString(), signature)) {
            logger.debug("ERROR : Abusing API Call!");
            return null;
        }

        try {
            logger.debug("Logging : replyMessage {}", request);
            LineRequest.Event event =  requestObj.getEvents().get(0);

            if (event.getType().equals("message")) {
                handleMessage(event);
            } else if (event.getType().equals("join")) {
                String groupId =  event.getSource().getGroupId();
                logger.debug("Join the group {}", groupId);
            }

        } catch (Exception ignore) {
            logger.debug("Exception occured in replyMessage", ignore);
        }

        LineResponse response = new LineResponse();
        try {
            response.setResponse(requestObj.getEvents().toString());
        } catch (Exception e) {
            logger.debug("ERROR : {}", e);
        }

        HttpResponse httpResponse = HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, requestObj.getEvents().toString());
        logger.debug("httpResponse <{}>", httpResponse);
        return httpResponse;
    }
}
