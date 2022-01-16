package net.mossol.bot.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "slack")
public class SlackBotSettings {
    String token;
    String signingSecret;
}
