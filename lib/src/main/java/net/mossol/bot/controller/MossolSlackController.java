package net.mossol.bot.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import net.mossol.bot.model.ReplyMessage;
import net.mossol.bot.model.TextType;
import net.mossol.bot.service.MessageHandler;
import net.mossol.bot.slack.ReconnectableBot;
import net.mossol.bot.util.MessageBuildUtil;

import me.ramswaroop.jbot.core.common.Controller;
import me.ramswaroop.jbot.core.common.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;

@Service
@ConditionalOnProperty(
        value = "service.enabled.slack",
        havingValue = "true",
        matchIfMissing = true

)
public class MossolSlackController extends ReconnectableBot {

    private static final Logger logger = LoggerFactory.getLogger(MossolSlackController.class);

    @Resource
    private MessageHandler messageHandler;

    @Value("${slackBotToken}")
    private String slackToken;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public ReconnectableBot getSlackBot() {
        return this;
    }

    private void handleMessage(WebSocketSession session, Event event) throws Exception {
        ReplyMessage replyMessage = messageHandler.replyMessage(event.getText());
        if (replyMessage == null) {
            logger.debug("INFO: there is no matching reply message");
            return;
        }

        TextType type = replyMessage.getType();

        switch(type) {
            case SELECT_MENU_K:
            case SELECT_MENU_J:
            case SELECT_MENU_D:
                reply(session, event,
                      new Message(MessageBuildUtil.sendFoodMessage(replyMessage.getLocationInfo())));
                return;
            case LEAVE_ROOM:
                break;
            default:
                reply(session, event, new Message(replyMessage.getText()));
                return;
        }

        throw new Exception("Send message failed");
    }

    @Controller(events = EventType.MESSAGE)
    public void onReceiveMessage(WebSocketSession session, Event event) {
        try {
            handleMessage(session, event);
        } catch (Exception e) {
            logger.warn("CTRL:SLACK exception is occurred", e);
        }
    }
}
