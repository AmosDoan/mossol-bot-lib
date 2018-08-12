package net.mossol.model;

import lombok.Data;

import java.util.regex.Pattern;

@Data
public class RegexText {
    public enum RegexTextType {
        ADD_MENU_K,
        ADD_MENU_J,
        ADD_MENU_D,
        DEL_MENU_K,
        DEL_MENU_J,
        DEL_MENU_D,
        TEXT;
    }

    private String regex;
    private RegexTextType type;
    private Pattern pattern;
    private String response;

    public RegexText compilePattern() {
        pattern = Pattern.compile(regex);
        return this;
    }
}
