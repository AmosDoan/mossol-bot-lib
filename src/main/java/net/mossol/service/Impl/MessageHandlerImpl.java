package net.mossol.service.Impl;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.linecorp.centraldogma.client.Watcher;
import net.mossol.model.SimpleText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mossol.HttpConnection;
import net.mossol.MossolUtil;
import net.mossol.model.LineReplyRequest;
import net.mossol.model.LineRequest;
import net.mossol.model.MenuInfo;
import net.mossol.service.MenuServiceHandler;
import net.mossol.service.MenuServiceHandler.FoodType;
import net.mossol.service.MessageHandler;
import net.mossol.util.MessageBuildUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static net.mossol.model.SimpleText.SimpleTextType.SHOW_MENU_D;
import static net.mossol.model.SimpleText.SimpleTextType.SHOW_MENU_J;
import static net.mossol.model.SimpleText.SimpleTextType.SHOW_MENU_K;

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

    private volatile Map<String, SimpleText> simpleTextContext;

    @Resource
    private MenuServiceHandler menuServiceHandler;

    @Resource
    private Watcher<Map<String, SimpleText>> simpleTextWatcher;

    @PostConstruct
    private void init() throws InterruptedException {
        simpleTextWatcher.watch((revision, context) -> {
            if (context == null)  {
                logger.warn("SimpleText Watch Failed");
                return;
            }
            logger.info("SimpleText Updated : " + context);
            simpleTextContext = context;
        });
    }

    private boolean sendFoodRequest(String token, FoodType foodType) {
        MenuInfo menu = menuServiceHandler.selectMenu(foodType);
        return sendRequest(REPLY_URI, MessageBuildUtil.sendFoodMessage(token, menu));
    }

    private static boolean sendRequest(String uri, Object request) {
        String payload = MossolUtil.writeJsonString(request);
        logger.debug("sendRequest Payload : {}", payload);
        return httpConnection.post(uri, payload);
    }

    private boolean simpleTextHandle(LineRequest.Event event, String token, SimpleText simpleText) {
        String menuCandidate;
        switch (simpleText.getType()) {
            case SHOW_MENU_K:
                menuCandidate = menuServiceHandler.getMenu(FoodType.KOREA_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, menuCandidate));
            case SHOW_MENU_J:
                menuCandidate = menuServiceHandler.getMenu(FoodType.JAPAN_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, menuCandidate));
            case SHOW_MENU_D:
                menuCandidate = menuServiceHandler.getMenu(FoodType.DRINK_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, menuCandidate));
            case TEXT:
                return sendRequest(REPLY_URI,
                        MessageBuildUtil.sendTextMessage(token, simpleText.getResponse()));
            case SELECT_MENU_K:
                return sendFoodRequest(token, FoodType.KOREA_FOOD);
            case SELECT_MENU_J:
                return sendFoodRequest(token, FoodType.JAPAN_FOOD);
            case SELECT_MENU_D:
                return sendFoodRequest(token, FoodType.DRINK_FOOD);
            case LEAVE_ROOM:
                String groupId =  event.getSource().getGroupId();
                String uri = String.format(LEAVE_URI, groupId);
                return sendRequest(uri, null);
        }
        return false;
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

            SimpleText simpleText = simpleTextContext.get(message);

            if (simpleText != null) {
                return simpleTextHandle(event, token, simpleText);
            }

            if (message.contains("안녕")) {
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, "멍멍!!"));
            } else if (addMatcher.find()) {
                String addMenuResult = menuServiceHandler.addMenu(addMatcher.group(), FoodType.KOREA_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, addMenuResult));
            } else if (removeMatcher.find()) {
                String removeMenuResult = menuServiceHandler.removeMenu(removeMatcher.group(),
                                                                        FoodType.KOREA_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, removeMenuResult));
            } else if (japanAddMatcher.find()) {
                String addMenuResult = menuServiceHandler.addMenu(japanAddMatcher.group(), FoodType.JAPAN_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, addMenuResult));
            } else if (japanRemoveMatcher.find()) {
                String removeMenuResult = menuServiceHandler.removeMenu(japanRemoveMatcher.group(),
                                                                        FoodType.JAPAN_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, removeMenuResult));
            } else if (drinkAddMatcher.find()) {
                String addMenuResult = menuServiceHandler.addMenu(japanAddMatcher.group(), FoodType.DRINK_FOOD);
                return sendRequest(REPLY_URI, MessageBuildUtil.sendTextMessage(token, addMenuResult));
            } else if (drinkRemoveMatcher.find()) {
                String removeMenuResult = menuServiceHandler.removeMenu(japanRemoveMatcher.group(),
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
