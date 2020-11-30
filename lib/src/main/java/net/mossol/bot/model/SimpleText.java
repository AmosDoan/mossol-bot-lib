package net.mossol.bot.model;

import lombok.Value;

@Value
public class SimpleText {
    String message;
    TextType type;
    String response;
}
