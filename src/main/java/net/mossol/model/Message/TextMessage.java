package net.mossol.model.Message;

import net.mossol.model.LineMessage;

import lombok.Data;

@Data
public class TextMessage extends LineMessage {
    private final String text;
}
