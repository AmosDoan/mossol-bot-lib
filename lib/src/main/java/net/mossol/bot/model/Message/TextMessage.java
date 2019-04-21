package net.mossol.bot.model.Message;

import net.mossol.bot.model.LineMessage;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TextMessage extends LineMessage {
    private final String text;
}
