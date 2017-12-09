package net.mossol.service.Impl;

import net.mossol.HttpConnection;
import net.mossol.MossolUtil;
import net.mossol.model.LineReplyRequest;
import net.mossol.model.LineRequest;
import net.mossol.service.LunchServiceHandler;
import net.mossol.service.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Service
public class MessageHandlerImpl implements MessageHandler {
    private static final String REPLY_URI = "https://api.line.me/v2/bot/message/reply";
    private static final String LEAVE_URI = "https://api.line.me/v2/bot/group/%s/leave";
    private static final HttpConnection httpConnection = new HttpConnection();

    @Autowired
    private LunchServiceHandler lunchServiceHandler;

    private boolean sendRequest(String uri, Object request) {
        return httpConnection.post(uri, MossolUtil.writeJsonString(request));
    }

    @Override
    public boolean replyMessage(LineRequest request) throws Exception {
        System.out.println("Logging : replyMessage" + request);
        LineRequest.Event event =  request.getEvents().get(0);

        if (event.getType().equals("message")) {
            String token = event.getReplyToken();
            String message = event.getMessage().getText();

            if (message.contains("안녕")) {
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText("멍멍!!");
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (message.equals("메뉴후보")) {
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText(lunchServiceHandler.getMenu());
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (message.equals("수안님께인사")) {
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText("수안님 밀크시슬 드세요 멍멍");
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (message.equals("메뉴골라줘")) {
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText(lunchServiceHandler.selectMenu());
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (message.equals("/집으로")) {
                String groupId =  event.getSource().getGroupId();
                String uri = String.format(LEAVE_URI, groupId);
                sendRequest(uri, null);
            }
        } else if (event.getType().equals("join")) {
            String groupId =  event.getSource().getGroupId();
            System.out.println("Join the group " + groupId);
            return true;
        }

        return false;
    }
}
