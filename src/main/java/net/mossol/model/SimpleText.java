package net.mossol.model;

import lombok.Value;

@Value
public class SimpleText {
    public enum SimpleTextType {
        SHOW_MENU_K,
        SHOW_MENU_J,
        SHOW_MENU_D,
        TEXT,
        SELECT_MENU_K,
        SELECT_MENU_J,
        SELECT_MENU_D,
        LEAVE_ROOM;
    }

    private String message;
    private SimpleTextType type;
    private String response;
}
