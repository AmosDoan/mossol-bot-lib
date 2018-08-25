package net.mossol.service.Impl;

import java.util.List;
import java.util.Map;

import com.linecorp.centraldogma.client.Watcher;
import net.mossol.connection.RetrofitConnection;
import net.mossol.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.mossol.MossolUtil;
import net.mossol.service.MenuServiceHandler;
import net.mossol.service.MenuServiceHandler.FoodType;
import net.mossol.service.MessageHandler;
import net.mossol.util.MessageBuildUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Service
public class MessageHandlerImpl implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);
    private static final String REPLY_URI = "https://api.line.me/v2/bot/message/reply";
    private static final String LEAVE_URI = "https://api.line.me/v2/bot/group/%s/leave";
    private volatile Map<String, SimpleText> simpleTextContext;
    private volatile List<RegexText> regexTextContext;

    @Resource
    private MenuServiceHandler menuServiceHandler;

    @Resource
    private Watcher<Map<String, SimpleText>> simpleTextWatcher;

    @Resource
    private Watcher<List<RegexText>> regexTextWatcher;

    @Resource
    private RetrofitConnection retrofitConnection;

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

        regexTextWatcher.watch((revision, context) -> {
            if (context == null)  {
                logger.warn("RegexText Watch Failed");
                return;
            }
            logger.info("RegexText Updated : " + context);
            regexTextContext = context;
        });
    }

    private boolean sendFoodRequest(String token, FoodType foodType) {
        MenuInfo menu = menuServiceHandler.selectMenu(foodType);
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

    private boolean regexTextHandle(String token, String message) {
        for (RegexText regex : regexTextContext) {
            String result = regex.match(message);
            String addMenuResult;
            if (!result.isEmpty()) {
                logger.debug("Regex Matched : message{}, match{}", message, result);
                switch (regex.getType()) {
                    case ADD_MENU_K:
                        addMenuResult = menuServiceHandler.addMenu(result, FoodType.KOREA_FOOD);
                        return sendRequest(MessageBuildUtil.sendTextMessage(token, addMenuResult));
                    case ADD_MENU_J:
                        addMenuResult = menuServiceHandler.addMenu(result, FoodType.JAPAN_FOOD);
                        return sendRequest(MessageBuildUtil.sendTextMessage(token, addMenuResult));
                    case ADD_MENU_D:
                        addMenuResult = menuServiceHandler.addMenu(result, FoodType.DRINK_FOOD);
                        return sendRequest(MessageBuildUtil.sendTextMessage(token, addMenuResult));
                    case DEL_MENU_K:
                        String removeMenuResult = menuServiceHandler.removeMenu(result, FoodType.KOREA_FOOD);
                        return sendRequest(MessageBuildUtil.sendTextMessage(token, removeMenuResult));
                    case DEL_MENU_J:
                        removeMenuResult = menuServiceHandler.removeMenu(result, FoodType.JAPAN_FOOD);
                        return sendRequest(MessageBuildUtil.sendTextMessage(token, removeMenuResult));
                    case DEL_MENU_D:
                        removeMenuResult = menuServiceHandler.removeMenu(result, FoodType.DRINK_FOOD);
                        return sendRequest(MessageBuildUtil.sendTextMessage(token, removeMenuResult));
                    case TEXT:
                        return sendRequest(MessageBuildUtil.sendTextMessage(token, regex.getResponse()));
                }
            }
        }
        return false;
    }

    private boolean simpleTextHandle(LineRequest.Event event, String token, SimpleText simpleText) {
        String menuCandidate;
        switch (simpleText.getType()) {
            case SHOW_MENU_K:
                menuCandidate = menuServiceHandler.getMenu(FoodType.KOREA_FOOD);
                return sendRequest(MessageBuildUtil.sendTextMessage(token, menuCandidate));
            case SHOW_MENU_J:
                menuCandidate = menuServiceHandler.getMenu(FoodType.JAPAN_FOOD);
                return sendRequest(MessageBuildUtil.sendTextMessage(token, menuCandidate));
            case SHOW_MENU_D:
                menuCandidate = menuServiceHandler.getMenu(FoodType.DRINK_FOOD);
                return sendRequest(MessageBuildUtil.sendTextMessage(token, menuCandidate));
            case TEXT:
                return sendRequest(MessageBuildUtil.sendTextMessage(token, simpleText.getResponse()));
            case SELECT_MENU_K:
                return sendFoodRequest(token, FoodType.KOREA_FOOD);
            case SELECT_MENU_J:
                return sendFoodRequest(token, FoodType.JAPAN_FOOD);
            case SELECT_MENU_D:
                return sendFoodRequest(token, FoodType.DRINK_FOOD);
            case LEAVE_ROOM:
                String groupId =  event.getSource().getGroupId();
                return leaveRoom(groupId);
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
            String simpleMessage = message.replaceAll("\\s+", "");

            SimpleText simpleText = simpleTextContext.get(simpleMessage);

            if (simpleText != null) {
                return simpleTextHandle(event, token, simpleText);
            }

            return regexTextHandle(token, message);
        } else if (event.getType().equals("join")) {
            String groupId =  event.getSource().getGroupId();
            logger.debug("Join the group {}", groupId);
            return true;
        }

        return false;
    }
}
