package net.mossol.bot.service.Impl;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.mossol.bot.model.RegexText;
import net.mossol.bot.model.ReplyMessage;
import net.mossol.bot.model.SimpleText;
import net.mossol.bot.service.KeiServiceHandler;
import net.mossol.bot.service.MenuServiceHandler;
import net.mossol.bot.service.MenuServiceHandler.FoodType;
import net.mossol.bot.service.MessageHandler;

import com.linecorp.centraldogma.client.Watcher;
import org.springframework.util.CollectionUtils;

import static net.mossol.bot.model.TextType.*;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Service
public class MessageHandlerImpl implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);
    private volatile Map<String, SimpleText> simpleTextContext;
    private volatile List<RegexText> regexTextContext;

    @Resource
    private MenuServiceHandler menuServiceHandler;

    @Resource
    private KeiServiceHandler keiServiceHandler;

    @Resource
    private Watcher<Map<String, SimpleText>> simpleTextWatcher;

    @Resource
    private Watcher<List<RegexText>> regexTextWatcher;

    @PostConstruct
    private void init() {
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

    private ReplyMessage regexTextHandle(String message) {
        for (RegexText regex : regexTextContext) {
            List<String> result = regex.match(message);
            if (!CollectionUtils.isEmpty(result)) {
                logger.debug("Regex Matched : message{}, match{}", message, result);
                switch (regex.getType()) {
                    case ADD_MENU_K:
                        return new ReplyMessage(ADD_MENU_K, null,
                                menuServiceHandler.addMenu(result, FoodType.KOREA_FOOD));
                    case ADD_MENU_J:
                        return new ReplyMessage(ADD_MENU_J, null,
                                menuServiceHandler.addMenu(result, FoodType.JAPAN_FOOD));
                    case ADD_MENU_D:
                        return new ReplyMessage(ADD_MENU_D, null,
                                menuServiceHandler.addMenu(result, FoodType.DRINK_FOOD));
                    case DEL_MENU_K:
                        return new ReplyMessage(DEL_MENU_K, null,
                                menuServiceHandler.removeMenu(result, FoodType.KOREA_FOOD));
                    case DEL_MENU_J:
                        return new ReplyMessage(DEL_MENU_J, null,
                                menuServiceHandler.removeMenu(result, FoodType.JAPAN_FOOD));
                    case DEL_MENU_D:
                        return new ReplyMessage(DEL_MENU_D, null,
                                menuServiceHandler.removeMenu(result, FoodType.DRINK_FOOD));
                    case RANDOM_SELECT:
                        return new ReplyMessage(RANDOM_SELECT, null, keiServiceHandler.getRandomMember(result));
                    case TEXT:
                        return new ReplyMessage(TEXT, null, regex.getResponse());
                }
            }
        }
        logger.debug("There is no matched regex for the message : " + message);
        return null;
    }

    private ReplyMessage simpleTextHandle(SimpleText simpleText) throws Exception {
        switch (simpleText.getType()) {
            case SHOW_MENU_K:
                return new ReplyMessage(SHOW_MENU_K, null, menuServiceHandler.getMenu(FoodType.KOREA_FOOD));
            case SHOW_MENU_J:
                return new ReplyMessage(SHOW_MENU_J, null, menuServiceHandler.getMenu(FoodType.JAPAN_FOOD));
            case SHOW_MENU_D:
                return new ReplyMessage(SHOW_MENU_D, null, menuServiceHandler.getMenu(FoodType.DRINK_FOOD));
            case TEXT:
                return new ReplyMessage(TEXT, null, simpleText.getResponse());
            case SELECT_MENU_K:
                return new ReplyMessage(SELECT_MENU_K, menuServiceHandler.selectMenu(FoodType.KOREA_FOOD), null);
            case SELECT_MENU_J:
                return new ReplyMessage(SELECT_MENU_J, menuServiceHandler.selectMenu(FoodType.JAPAN_FOOD), null);
            case SELECT_MENU_D:
                return new ReplyMessage(SELECT_MENU_D, menuServiceHandler.selectMenu(FoodType.DRINK_FOOD), null);
            case LEAVE_ROOM:
                return new ReplyMessage(LEAVE_ROOM, null, null);
            case KEI_CS:
                return new ReplyMessage(KEI_CS, null, keiServiceHandler.getCSLotto());
        }

        throw new Exception("Invalid SimpleText Type");
    }

    @Override
    public ReplyMessage replyMessage(String requestMessage) throws Exception {
        final String simpleMessage = requestMessage.replaceAll("\\s+", "");
        final SimpleText simpleText = simpleTextContext.get(simpleMessage);

        if (simpleText != null) {
            return simpleTextHandle(simpleText);
        }

        return regexTextHandle(simpleMessage);
    }
}
