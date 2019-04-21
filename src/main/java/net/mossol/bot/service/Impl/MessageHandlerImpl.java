package net.mossol.bot.service.Impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.mossol.bot.model.ReplyMessage;
import net.mossol.bot.service.MatcherService;
import net.mossol.bot.service.MessageHandler;

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
