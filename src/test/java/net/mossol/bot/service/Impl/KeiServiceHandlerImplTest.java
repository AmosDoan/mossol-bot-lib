package net.mossol.bot.service.Impl;

import net.mossol.bot.model.RegexText;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class KeiServiceHandlerImplTest {

    private KeiServiceHandlerImpl handler = new KeiServiceHandlerImpl();

    @Test
    public void testRandom() {
        RegexText regexText = new RegexText();
        regexText.setRegex("(.+(?=에서.*명골라줘)|(\\d+))");
        regexText.compilePattern();

        final String message = "석영태,이경찬,김도한에서 0명 골라줘";
        final String expected = "멍멍! 골라야하는 분들 수가 이상해yo!";
        final String simpleMessage = message.replaceAll("\\s+", "");
        final List<String> matchResult = regexText.match(simpleMessage);

        final String result = handler.getRandomMember(matchResult);
        assertEquals(expected, result);
    }
}
