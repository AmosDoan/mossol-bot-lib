package net.mossol.model.Message;

import net.mossol.model.LineMessage;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TextMessage extends LineMessage {
    private final String text;
}
