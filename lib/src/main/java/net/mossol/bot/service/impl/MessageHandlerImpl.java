package net.mossol.bot.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.mossol.bot.model.ReplyMessage;
import net.mossol.bot.service.MatcherService;
import net.mossol.bot.service.MessageHandler;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
@Service
public class MessageHandlerImpl implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);

    @Resource
    private List<MatcherService> matcherServices;

    @Override
    public ReplyMessage replyMessage(String requestMessage) throws Exception {
        if (StringUtils.isEmpty(requestMessage)) {
            return null;
        }

        for (MatcherService service : matcherServices) {
            ReplyMessage replyMessage = service.match(requestMessage);
            if (replyMessage != null) {
                return replyMessage;
            }
        }

        logger.info("No matched message");
        return null;
    }
}
