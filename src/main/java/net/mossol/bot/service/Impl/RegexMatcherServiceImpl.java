package net.mossol.bot.service.Impl;

import static net.mossol.bot.model.TextType.ADD_MENU_D;
import static net.mossol.bot.model.TextType.ADD_MENU_J;
import static net.mossol.bot.model.TextType.ADD_MENU_K;
import static net.mossol.bot.model.TextType.DEL_MENU_D;
import static net.mossol.bot.model.TextType.DEL_MENU_J;
import static net.mossol.bot.model.TextType.DEL_MENU_K;
import static net.mossol.bot.model.TextType.RANDOM_SELECT;
import static net.mossol.bot.model.TextType.TEXT;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import net.mossol.bot.model.RegexText;
import net.mossol.bot.model.ReplyMessage;
import net.mossol.bot.service.KeiServiceHandler;
import net.mossol.bot.service.MatcherService;
import net.mossol.bot.service.MenuServiceHandler;
import net.mossol.bot.service.MenuServiceHandler.FoodType;

import com.linecorp.centraldogma.client.Watcher;

@Service
@Order(2)
public class RegexMatcherServiceImpl implements MatcherService {
    private static final Logger logger = LoggerFactory.getLogger(RegexMatcherServiceImpl.class);
    private volatile List<RegexText> regexTextContext;

    @Resource
    private Watcher<List<RegexText>> regexTextWatcher;

    @Resource
    private MenuServiceHandler menuServiceHandler;

    @Resource
    private KeiServiceHandler keiServiceHandler;

    @PostConstruct
    private void init() {
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

    @Override
    public ReplyMessage match(String requestMessage) {
        return regexTextHandle(requestMessage.replaceAll("\\s+", ""));
    }
}
