package net.mossol.model.Message;

import net.mossol.model.LineMessage;

import lombok.Data;

@Data
public class LocationMessage extends LineMessage {
    private final String title;
    private final String address;
    private final double latitude;
    private final double longitude;
}
