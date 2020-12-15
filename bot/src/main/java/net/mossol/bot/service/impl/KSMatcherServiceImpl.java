package net.mossol.bot.service.impl;

import static net.mossol.bot.model.TextType.KEI_CS;
import static net.mossol.bot.model.TextType.RANDOM_SELECT;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import net.mossol.bot.model.RegexText;
import net.mossol.bot.model.ReplyMessage;
import net.mossol.bot.service.KSServiceHandler;
import net.mossol.bot.service.MatcherService;

import com.linecorp.centraldogma.client.Watcher;

@Service
@Order(3)
public class KSMatcherServiceImpl implements MatcherService {
    private static final Logger logger = LoggerFactory.getLogger(KSMatcherServiceImpl.class);
    private volatile List<RegexText> kSRegexTextContext;

    @Resource
    private KSServiceHandler kSServiceHandler;

    @Resource
    private Watcher<List<RegexText>> kSRegexTextWatcher;

    @PostConstruct
    private void init() {
        kSRegexTextWatcher.watch((revision, context) -> {
            if (context == null)  {
                logger.warn("KSRegexText Watch Failed");
                return;
            }
            logger.info("KSRegexText Updated : {}, rev<{}>", context, revision);
            kSRegexTextContext = context;
        });
    }

    private ReplyMessage regexTextHandle(String message) {
        for (RegexText regex : kSRegexTextContext) {
            List<String> result = regex.match(message);
            if (!CollectionUtils.isEmpty(result)) {
                logger.debug("Regex Matched : message{}, match{}", message, result);
                switch (regex.getType()) {
                    case RANDOM_SELECT:
                        return new ReplyMessage(RANDOM_SELECT, null, kSServiceHandler.getRandomMember(result));
                    case KEI_CS:
                        return new ReplyMessage(KEI_CS, null, kSServiceHandler.getCSLotto());
                }
            }
        }

        logger.debug("There is no matched KSregex for the message : {}", message);
        return null;
    }

    @Override
    public ReplyMessage match(String requestMessage) throws Exception {
        ReplyMessage replyMessage = regexTextHandle(requestMessage.replaceAll("\\s+", ""));
        if (replyMessage != null) {
            return replyMessage;
        }

        logger.info("KSMatcher: There is no matched request message");
        return null;
    }
}
