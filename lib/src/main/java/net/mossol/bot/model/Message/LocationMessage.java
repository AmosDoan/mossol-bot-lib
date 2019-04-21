package net.mossol.bot.model.Message;

import net.mossol.bot.model.LineMessage;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class LocationMessage extends LineMessage {
    private final String title;
    private final String address;
    private final double latitude;
    private final double longitude;
}
