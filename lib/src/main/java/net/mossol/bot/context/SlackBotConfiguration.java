package net.mossol.bot.context;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.mossol.bot.service.MessageHandler;
import net.mossol.bot.settings.SlackBotSettings;
import net.mossol.bot.slack.SlackService;

import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.rtm.RTMClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConditionalOnProperty(
        value = "slack.enable",
        havingValue = "true",
        matchIfMissing = true
)
@Configuration
public class SlackBotConfiguration {

    @Bean
    public AsyncMethodsClient slackClient(SlackBotSettings slackBotSettings) {
        final AppConfig appConfig = AppConfig.builder()
                                             .singleTeamBotToken(slackBotSettings.getToken())
                                             .build();

        log.debug("initialize slack app token <{}>;", slackBotSettings.getToken());

        return new App(appConfig).slack().methodsAsync(appConfig.getSingleTeamBotToken());
    }

    @Bean
    public RTMClient rtmClient(SlackBotSettings settings) throws IOException {
        final Slack slack = Slack.getInstance();
        return slack.rtmConnect(settings.getToken());
    }

    @Bean
    public SlackService slackService(AsyncMethodsClient slackClient, RTMClient rtmClient,
                                     MessageHandler messageHandler) {
        return new SlackService(slackClient, rtmClient, messageHandler);
    }
}
