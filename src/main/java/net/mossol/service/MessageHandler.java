package net.mossol.service;

import net.mossol.model.LineRequest;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 6..
 */
public interface MessageHandler {

    boolean replyMessage(LineRequest request) throws Exception;
}
