package net.mossol.bot.bot.service;

import net.mossol.bot.bot.model.ReplyMessage;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public interface MessageHandler {

    ReplyMessage replyMessage(String requestMessage) throws Exception;
}
