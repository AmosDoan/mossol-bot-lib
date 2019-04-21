package net.mossol.bot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
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

    public List<String> match(String message) {
        Matcher matcher = pattern.matcher(message);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }
}
