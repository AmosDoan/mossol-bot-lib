package net.mossol;

import com.fasterxml.jackson.databind.JsonNode;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.annotation.*;
import net.mossol.model.LineRequest;
import net.mossol.model.LineResponse;
import net.mossol.service.MessageHandler;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Amos.Doan.Mac on 2017. 11. 18..
 */
@Service
public class MossolController {
    private static final Logger logger = LoggerFactory.getLogger(MossolController.class);
    private static final String template = "%dth, Hello, %s!";
    private static final String SECRET_KEY = "49588e53b2c64f47a3fc84739e17b757";

    private final AtomicLong counter = new AtomicLong();

    @Resource
    private MessageHandler messageHandler;

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
            messageHandler.replyMessage(requestObj);
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
