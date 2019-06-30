package net.mossol.bot.service.impl;

import static net.mossol.bot.model.TextType.LEAVE_CHAT;
import static net.mossol.bot.model.TextType.SELECT_MENU_D;
import static net.mossol.bot.model.TextType.SELECT_MENU_J;
import static net.mossol.bot.model.TextType.SELECT_MENU_K;
import static net.mossol.bot.model.TextType.SHOW_MENU_D;
import static net.mossol.bot.model.TextType.SHOW_MENU_J;
import static net.mossol.bot.model.TextType.SHOW_MENU_K;
import static net.mossol.bot.model.TextType.TEXT;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import net.mossol.bot.model.ReplyMessage;
import net.mossol.bot.model.SimpleText;
import net.mossol.bot.service.MatcherService;
import net.mossol.bot.service.MenuServiceHandler;
import net.mossol.bot.model.MenuType;

import com.linecorp.centraldogma.client.Watcher;

@Service
@Order(1)
public class SimpleMatcherServiceImpl implements MatcherService {
    private static final Logger logger = LoggerFactory.getLogger(SimpleMatcherServiceImpl.class);
    private volatile Map<String, SimpleText> simpleTextContext;

    @Resource
    private MenuServiceHandler menuServiceHandler;

    @Resource
    private Watcher<Map<String, SimpleText>> simpleTextWatcher;

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
    }

    private ReplyMessage simpleTextHandle(SimpleText simpleText) throws Exception {
        switch (simpleText.getType()) {
            case SHOW_MENU_K:
                return new ReplyMessage(SHOW_MENU_K, null, menuServiceHandler.getMenu(MenuType.KOREA_MENU));
            case SHOW_MENU_J:
                return new ReplyMessage(SHOW_MENU_J, null, menuServiceHandler.getMenu(MenuType.JAPAN_MENU));
            case SHOW_MENU_D:
                return new ReplyMessage(SHOW_MENU_D, null, menuServiceHandler.getMenu(MenuType.KOREA_DRINK_MENU));
            case TEXT:
                return new ReplyMessage(TEXT, null, simpleText.getResponse());
            case SELECT_MENU_K:
                return new ReplyMessage(SELECT_MENU_K, menuServiceHandler.selectMenu(MenuType.KOREA_MENU), null);
            case SELECT_MENU_J:
                return new ReplyMessage(SELECT_MENU_J, menuServiceHandler.selectMenu(MenuType.JAPAN_MENU), null);
            case SELECT_MENU_D:
                return new ReplyMessage(SELECT_MENU_D, menuServiceHandler.selectMenu(MenuType.KOREA_DRINK_MENU), null);
            case LEAVE_CHAT:
                return new ReplyMessage(LEAVE_CHAT, null, simpleText.getResponse());
        }

        logger.debug("There is no matched simple for the message : " + simpleText);
        return null;
    }

    @Override
    public ReplyMessage match(String requestMessage) throws Exception {
        if (StringUtils.isEmpty(requestMessage)) {
            return null;
        }

        final String simpleMessage = requestMessage.replaceAll("\\s+", "");
        final SimpleText simpleText = simpleTextContext.get(simpleMessage);

        if (simpleText != null) {
            return simpleTextHandle(simpleText);
        }

        return null;
    }
}
