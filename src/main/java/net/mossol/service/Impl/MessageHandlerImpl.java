package net.mossol.service.Impl;

import net.mossol.HttpConnection;
import net.mossol.MossolUtil;
import net.mossol.model.LineReplyRequest;
import net.mossol.model.LineRequest;
import net.mossol.service.LunchServiceHandler;
import net.mossol.service.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Service
public class MessageHandlerImpl implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);
    private static final String REPLY_URI = "https://api.line.me/v2/bot/message/reply";
    private static final String LEAVE_URI = "https://api.line.me/v2/bot/group/%s/leave";
    private static final HttpConnection httpConnection = new HttpConnection();
    private static final Pattern ADD_PATTERN = Pattern.compile("(?<=^메뉴추가\\s)(.+$)");
    private static final Pattern REMOVE_PATTERN = Pattern.compile("(?<=^메뉴삭제\\s)(.+)");
    private static final Pattern JAPAN_ADD_PATTERN = Pattern.compile("(?<=^일본메뉴추가\\s)(.+$)");
    private static final Pattern JAPAN_REMOVE_PATTERN = Pattern.compile("(?<=^일본메뉴삭제\\s)(.+$)");

    @Autowired
    private LunchServiceHandler lunchServiceHandler;

    private static boolean sendRequest(String uri, Object request) {
        return httpConnection.post(uri, MossolUtil.writeJsonString(request));
    }

    @Override
    public boolean replyMessage(LineRequest request) throws Exception {
        logger.debug("Logging : replyMessage {}", request);
        LineRequest.Event event =  request.getEvents().get(0);

        if (event.getType().equals("message")) {
            String token = event.getReplyToken();
            String message = event.getMessage().getText();

            Matcher addMatcher = ADD_PATTERN.matcher(message);
            Matcher removeMatcher = REMOVE_PATTERN.matcher(message);
            Matcher japanAddMatcher = JAPAN_ADD_PATTERN.matcher(message);
            Matcher japanRemoveMatcher = JAPAN_REMOVE_PATTERN.matcher(message);

            message = message.replaceAll("\\s+", "");

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
                replyMessage.setText(lunchServiceHandler.getMenu(LunchServiceHandler.FoodType.KOREA_FOOD));
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (message.equals("일본메뉴후보")) {
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText(lunchServiceHandler.getMenu(LunchServiceHandler.FoodType.JAPAN_FOOD));
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
            } else if (message.equals("헐")) {
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText("헐 멍멍!!");
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (message.equals("다녀오세요")) {
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText("카이지상이 다녀오세요 멍멍!!");
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (message.equals("메뉴골라줘")) {
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText(lunchServiceHandler.selectMenu(LunchServiceHandler.FoodType.KOREA_FOOD));
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (message.equals("일본메뉴골라줘")) {
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText(lunchServiceHandler.selectMenu(LunchServiceHandler.FoodType.JAPAN_FOOD));
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (message.equals("/집으로")) {
                String groupId =  event.getSource().getGroupId();
                String uri = String.format(LEAVE_URI, groupId);
                sendRequest(uri, null);
            } else if (addMatcher.find()) {
                String food = addMatcher.group();
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText(lunchServiceHandler.addMenu(food, LunchServiceHandler.FoodType.KOREA_FOOD));
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (removeMatcher.find()) {
                String food = removeMatcher.group();
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText(lunchServiceHandler.removeMenu(food, LunchServiceHandler.FoodType.KOREA_FOOD));
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (japanAddMatcher.find()) {
                String food = japanAddMatcher.group();
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText(lunchServiceHandler.addMenu(food, LunchServiceHandler.FoodType.JAPAN_FOOD));
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            } else if (japanRemoveMatcher.find()) {
                String food = japanRemoveMatcher.group();
                LineReplyRequest replyRequest = new LineReplyRequest();
                replyRequest.setReplyToken(token);

                LineReplyRequest.Message replyMessage = new LineReplyRequest.Message();
                replyMessage.setText(lunchServiceHandler.removeMenu(food, LunchServiceHandler.FoodType.JAPAN_FOOD));
                replyMessage.setType("text");
                replyRequest.setMessage(replyMessage);
                return sendRequest(REPLY_URI, replyRequest);
            }
        } else if (event.getType().equals("join")) {
            String groupId =  event.getSource().getGroupId();
            System.out.println("Join the group " + groupId);
            return true;
        }

        return false;
    }
}
