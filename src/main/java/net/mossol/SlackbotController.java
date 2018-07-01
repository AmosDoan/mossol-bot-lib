package net.mossol;

import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import net.mossol.model.MenuInfo;
import net.mossol.service.MenuServiceHandler;
import net.mossol.util.MessageBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
public class SlackbotController extends Bot {

    private static final Logger logger = LoggerFactory.getLogger(SlackbotController.class);

    @Autowired
    private MenuServiceHandler menuServiceHandler;

    @Value("${slackBotToken}")
    private String slackToken;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    @Controller(events = EventType.MESSAGE)
    public void onReceiveMessage(WebSocketSession session, Event event) {

        String message = event.getText().replaceAll("\\s+", "");

        if (message.contains("안녕")) {
            reply(session, event, new Message("멍멍!!"));
            return;
        } else if (message.contains("헐")) {
            reply(session, event, new Message("헐!! 멍멍!!"));
            return;
        } else if (message.equals("메뉴후보") || message.equals("메뉴보여줘")) {
            String menuCandidate = menuServiceHandler.getMenu(MenuServiceHandler.FoodType.KOREA_FOOD);
            reply(session, event, new Message(menuCandidate));
        } else if ((message.equals("메뉴골라줘") || message.equals("메뉴"))) {
            MenuInfo menu = menuServiceHandler.selectMenu(MenuServiceHandler.FoodType.KOREA_FOOD);
            reply(session, event, new Message(MessageBuildUtil.sendFoodMessage(menu)));
        }
    }

}
