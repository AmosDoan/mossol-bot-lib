package net.mossol;

import com.fasterxml.jackson.databind.JsonNode;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.annotation.Path;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.RequestObject;
import io.netty.util.internal.StringUtil;
import net.mossol.model.ReplyMessage;
import net.mossol.model.TextType;
import net.mossol.service.MessageHandler;
import net.mossol.util.MessageBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class MossolMessageController {
    private static final Logger logger = LoggerFactory.getLogger(MossolMessageController.class);

    @Resource
    private MessageHandler messageHandler;

    @Post
    @Path("/getMessage")
    public HttpResponse getMessage(@RequestObject JsonNode request) {
        final String message = request.get("message").textValue();
        final Map<String, String> ret = new HashMap<>();
        HttpResponse httpResponse;

        ReplyMessage replyMessage;
        try {
            replyMessage = messageHandler.replyMessage(message);
            if (replyMessage == null) {
                logger.debug("INFO: there is no matching reply message");
                throw new Exception();
            }
        } catch (Exception e) {
            httpResponse = HttpResponse.of(HttpStatus.NOT_FOUND, MediaType.JSON_UTF_8, StringUtil.EMPTY_STRING);
            logger.debug("httpResponse <{}>", httpResponse);
            return httpResponse;
        }

        TextType type = replyMessage.getType();

        String response;
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
            case KEI_CS:
                ret.put("message", replyMessage.getText());
                response = MossolUtil.writeJsonString(ret);
                httpResponse = HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, response);
                return httpResponse;
            case SELECT_MENU_K:
            case SELECT_MENU_J:
            case SELECT_MENU_D:
                final String foodMessage = MessageBuildUtil.sendFoodMessage(replyMessage.getMenuInfo());
                ret.put("message", foodMessage);
                response = MossolUtil.writeJsonString(ret);
                httpResponse = HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, response);
                return httpResponse;
            case LEAVE_ROOM:
                break;
        }

        httpResponse = HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR);
        return httpResponse;
    }
}
