package net.mossol.bot.slack;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.websocket.DeploymentException;

import net.mossol.bot.model.ReplyMessage;
import net.mossol.bot.service.MessageHandler;
import net.mossol.bot.util.MessageBuildUtil;

import com.google.common.util.concurrent.RateLimiter;
import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.rtm.RTMClient;
import com.slack.api.rtm.RTMEventHandler;
import com.slack.api.rtm.RTMEventsDispatcher;
import com.slack.api.rtm.RTMEventsDispatcherFactory;
import com.slack.api.rtm.message.PingMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlackService implements Closeable {

    private final AsyncMethodsClient slackClient;

    private final RTMClient rtmClient;

    private final ScheduledExecutorService executorService;

    private final AtomicLong pingId = new AtomicLong();

    private final MessageHandler messageHandler;

    private RateLimiter rateLimiter;

    class MessageEventHandler extends RTMEventHandler<MessageEvent>  {
        @Override
        public void handle(MessageEvent event) {
            log.debug("Received text<{}> from channel <{}> thread <{}>", event.getText(), event.getChannel(),
                      event.getThreadTs());

            final ReplyMessage reply;
            try {
                reply = messageHandler.replyMessage(event.getText());
                if (reply == null) {
                    return;
                }
            } catch (Exception ignored) {
                return;
            }

            final String replyText;
            switch(reply.getType()) {
                case SELECT_MENU_K:
                case SELECT_MENU_J:
                case SELECT_MENU_D:
                    replyText = MessageBuildUtil.sendFoodMessage(reply.getLocationInfo());
                    break;
                case LEAVE_CHAT:
                    return;
                default:
                    replyText = reply.getText();
            }

            rateLimiter.acquire();
            final ChatPostMessageRequest chatPostMessageRequest =
                    ChatPostMessageRequest.builder().channel(event.getChannel())
                                          .text(replyText)
                                          .threadTs(event.getThreadTs())
                                          .build();
            slackClient.chatPostMessage(chatPostMessageRequest);
        }
    }

    class Ping implements Runnable {
        @Override
        public void run() {
            try {
                log.debug("Pinging Slack... {}th", pingId.get());
                rtmClient.sendMessage(PingMessage.builder()
                                                 .id(pingId.incrementAndGet())
                                                 .build()
                                                 .toJSONString());
            } catch (Exception e) {
                log.error("Error pinging Slack. Slack bot may go offline when not active. Exception: ", e);
                connectToSlack();
            }
        }
    }

    private void connectToSlack() {
        try {
            rtmClient.connect();
        } catch (Exception e) {
            log.error("Failed to connect to slack");
        }
    }

    public SlackService(AsyncMethodsClient slackClient, RTMClient rtmClient,
                        MessageHandler messageHandler) {
        this.slackClient = slackClient;
        this.rtmClient = rtmClient;
        this.messageHandler = messageHandler;

        executorService = Executors.newSingleThreadScheduledExecutor();

        final RTMEventsDispatcher dispatcher = RTMEventsDispatcherFactory.getInstance();
        dispatcher.register(new MessageEventHandler());
        rtmClient.addMessageHandler(dispatcher.toMessageHandler());

        connectToSlack();

        executorService.scheduleAtFixedRate(new Ping(), 1L, 30L, TimeUnit.SECONDS);

        rateLimiter = RateLimiter.create(1L);
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
        rtmClient.disconnect();
    }

    public void push(String channel, String text) {
        final ChatPostMessageRequest chatPostMessageRequest =
                ChatPostMessageRequest.builder().channel(channel)
                                      .text(text)
                                      .build();

        rateLimiter.acquire();
        slackClient.chatPostMessage(chatPostMessageRequest);
    }
}
