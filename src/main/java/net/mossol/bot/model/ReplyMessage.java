package net.mossol.bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReplyMessage {
    private final TextType type;
    private final MenuInfo menuInfo;
    private final String text;
}
