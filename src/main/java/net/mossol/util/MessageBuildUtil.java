package net.mossol.util;

import net.mossol.model.LineReplyRequest;
import net.mossol.model.Message.LocationMessage;
import net.mossol.model.Message.TextMessage;

public class MessageBuildUtil {

    private MessageBuildUtil() {

    }

    public static LineReplyRequest sendTextMessage(String token, String content) {
        TextMessage replyMessage = new TextMessage(content);
        replyMessage.setType("text");

        LineReplyRequest replyRequest = new LineReplyRequest(token);
        replyRequest.setMessage(replyMessage);
        return replyRequest;
    }

    public static LineReplyRequest sendLocationMessage(String token, String content, String title,
                                                       String address, double latitude, double longitude) {
        LocationMessage locationMessage = new LocationMessage(title, address, latitude, longitude);
        locationMessage.setType("location");

        TextMessage replyMessage = new TextMessage(content);
        replyMessage.setType("text");

        LineReplyRequest replyRequest = new LineReplyRequest(token);
        replyRequest.setMessage(replyMessage);
        replyRequest.setMessage(locationMessage);
        return replyRequest;
    }
}
