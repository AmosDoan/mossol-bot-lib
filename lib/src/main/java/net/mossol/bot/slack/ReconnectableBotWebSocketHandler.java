package net.mossol.bot.slack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public class ReconnectableBotWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReconnectableBotWebSocketHandler.class);

    private ReconnectableBot bot;

    public ReconnectableBotWebSocketHandler(ReconnectableBot bot) {
        this.bot = bot;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        bot.afterConnectionEstablished(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        bot.handleTextMessage(session, message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        logger.error("Binary messages are not supported in Slack RTM API");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        bot.afterConnectionClosed(session, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        bot.handleTransportError(session, exception);
    }
}
