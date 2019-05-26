package net.mossol.bot.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class LinePushRequest {
    private final String to;
    private List<LineMessage> messages = new ArrayList<>();
    private boolean notificationDisabled;

    public void setMessage(LineMessage message) {
        messages.add(message);
    }
}
