package net.mossol.service.Impl;

import net.mossol.HttpConnection;
import net.mossol.MossolUtil;
import net.mossol.model.LineReplyRequest;
import net.mossol.model.LineRequest;
import net.mossol.model.LineRequest.Message;
import net.mossol.model.LocationInfo;
import net.mossol.model.Message.TextMessage;
import net.mossol.service.LunchServiceHandler;
import net.mossol.service.LunchServiceHandler.FoodType;
import net.mossol.service.MessageHandler;
import net.mossol.util.MessageBuildUtil;

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
    private static final Pattern DRINK_ADD_PATTERN = Pattern.compile("(?<=^회식추가\\s)(.+$)");
    private static final Pattern DRINK_REMOVE_PATTERN = Pattern.compile("(?<=^회식삭제\\s)(.+)");

    @Autowired
    private LunchServiceHandler lunchServiceHandler;

    private boolean sendFoodRequest(String token, FoodType foodType) {
        String todayMenu = lunchServiceHandler.selectMenu(foodType);
        LocationInfo locationInfo = lunchServiceHandler.getLocationInfo(todayMenu);
        String format = lunchServiceHandler.getSelectedMenuFormat(todayMenu);
        if (locationInfo != null) {
            return sendRequest(REPLY_URI, MessageBuildUtil.sendFoodMessage(token, format, locationInfo));
        } else {
            return sendRequest(REPLY_URI, MessageBuildUtil.sendFoodMessage(token, format));
        }
    }

    private static boolean sendRequest(String uri, Object request) {
        String payload = MossolUtil.writeJsonString(request);
        logger.debug("sendRequest Payload : {}", payload);
        return httpConnection.post(uri, payload);
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
            Matcher drinkAddMatcher = DRINK_ADD_PATTERN.matcher(message);
            Matcher drinkRemoveMatcher = DRINK_REMOVE_PATTERN.matcher(message);

            message = message.replaceAll("\\s+", "");

            if (message.contains("안녕")) {
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, "멍멍!!"));
            } else if (message.equals("메뉴후보") || message.equals("메뉴보여줘")) {
                String menuCandidate = lunchServiceHandler.getMenu(FoodType.KOREA_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, menuCandidate));
            } else if (message.equals("일본메뉴후보")) {
                String menuCandidate = lunchServiceHandler.getMenu(FoodType.JAPAN_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, menuCandidate));
            } else if (message.equals("회식장소")) {
                String menuCandidate = lunchServiceHandler.getMenu(FoodType.DRINK_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, menuCandidate));
            } else if (message.equals("수안님께인사")) {
                return sendRequest(REPLY_URI,
                                   MessageBuildUtil.sendTextMessage(token, "수안님 밀크시슬 드세요 멍멍"));
            } else if (message.equals("희승님께인사")) {
                return sendRequest(REPLY_URI,
                                   MessageBuildUtil.sendTextMessage(token, "희승님 아르메리아 PR 머지해주세요 멍멍!"));
            } else if (message.equals("헐")) {
                return sendRequest(REPLY_URI,
                                   MessageBuildUtil.sendTextMessage(token, "헐 멍멍!!"));
            } else if (message.equals("다녀오세요")) {
                return sendRequest(REPLY_URI,
                                   MessageBuildUtil.sendTextMessage(token, "카이지상이 다녀오세요 멍멍!!"));
            } else if (message.equals("메뉴골라줘") || message.equals("메뉴")) {
                return sendFoodRequest(token, FoodType.KOREA_FOOD);
            } else if (message.equals("일본메뉴골라줘")) {
                return sendFoodRequest(token, FoodType.JAPAN_FOOD);
            } else if (message.equals("회식장소골라줘")) {
                return sendFoodRequest(token, FoodType.DRINK_FOOD);
            } else if (message.equals("테스트!@")) {
                logger.debug("TEST");
                LineReplyRequest locationRequest =
                    MessageBuildUtil.sendLocationMessage(token, "우리집", "복정동 641번지",
                                                         37.467185, 127.127161);
                return sendRequest(REPLY_URI, locationRequest);
            } else if (message.equals("/집으로")) {
                String groupId =  event.getSource().getGroupId();
                String uri = String.format(LEAVE_URI, groupId);
                sendRequest(uri, null);
            } else if (addMatcher.find()) {
                String addMenuResult = lunchServiceHandler.addMenu(addMatcher.group(), FoodType.KOREA_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, addMenuResult));
            } else if (removeMatcher.find()) {
                String removeMenuResult = lunchServiceHandler.removeMenu(removeMatcher.group(),
                                                                         FoodType.KOREA_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, removeMenuResult));
            } else if (japanAddMatcher.find()) {
                String addMenuResult = lunchServiceHandler.addMenu(japanAddMatcher.group(), FoodType.JAPAN_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, addMenuResult));
            } else if (japanRemoveMatcher.find()) {
                String removeMenuResult = lunchServiceHandler.removeMenu(japanRemoveMatcher.group(),
                                                                         FoodType.JAPAN_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, removeMenuResult));
            } else if (drinkAddMatcher.find()) {
                String addMenuResult = lunchServiceHandler.addMenu(japanAddMatcher.group(), FoodType.DRINK_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, addMenuResult));
            } else if (drinkRemoveMatcher.find()) {
                String removeMenuResult = lunchServiceHandler.removeMenu(japanRemoveMatcher.group(),
                                                                         FoodType.DRINK_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, removeMenuResult));
            }
        } else if (event.getType().equals("join")) {
            String groupId =  event.getSource().getGroupId();
            logger.debug("Join the group {}", groupId);
            return true;
        }

        return false;
    }
}
