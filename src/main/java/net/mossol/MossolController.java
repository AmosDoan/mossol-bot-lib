package net.mossol;

import net.mossol.model.HealthResponse;
import net.mossol.model.LineRequest;
import net.mossol.model.LineResponse;
import net.mossol.service.MessageHandler;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Amos.Doan.Mac on 2017. 11. 18..
 */
@RestController
public class MossolController {
    private static final Logger logger = LoggerFactory.getLogger(MossolController.class);
    private static final String template = "Hello, %s!";
    private static final String SECRET_KEY = "49588e53b2c64f47a3fc84739e17b757";

    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private MessageHandler messageHandler;

    private boolean validateHeader(String requestBody) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(requestBody.getBytes()));
            logger.debug("Hash Result {}", hash);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @RequestMapping("/healthCheck")
    public HealthResponse healthCheck(@RequestParam(value = "name", defaultValue = "world")String name) {
        logger.debug("health check");
        return new HealthResponse(counter.incrementAndGet(), String.format(template, name));
    }

    @RequestMapping(value = "/line", method = RequestMethod.POST)
    public LineResponse getLine(@RequestHeader(value = "X-Line-Signature") String signature,
                                @RequestBody String request) {
        LineRequest requestObj = MossolUtil.readJsonString(request);

        if (requestObj == null) {
            return null;
        }

        LineResponse response = new LineResponse();

        if(!validateHeader(request)) {
            logger.debug("ERROR : Abusing API Call!");
            return null;
        }

        try {
            response.setResponse(requestObj.getEvents().toString());
        } catch (Exception e) {
            logger.debug("ERROR : {}", e);
        }

        try {
            messageHandler.replyMessage(requestObj);
        } catch (Exception ignore) {
            logger.debug("Exception occured in replyMessage");
        }

        return response;
    }
}
