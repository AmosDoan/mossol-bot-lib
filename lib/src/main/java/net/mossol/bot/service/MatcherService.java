package net.mossol.bot.service;

import net.mossol.bot.model.ReplyMessage;

public interface MatcherService {
    ReplyMessage match(String requestMessage) throws Exception;
}