package net.mossol.bot.model;

import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class RegexText {
    private String regex;
    private TextType type;
    private Pattern pattern;
    private String response;

    public RegexText compilePattern() {
        pattern = Pattern.compile(regex);
        return this;
    }

    public String match(String message) {
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
