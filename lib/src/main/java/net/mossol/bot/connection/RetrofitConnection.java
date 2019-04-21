package net.mossol.bot.connection;

import net.mossol.bot.model.LineReplyRequest;

public interface RetrofitConnection {
    void sendReply(LineReplyRequest request);

    void leaveRoom(LineReplyRequest request, String groupId);
}
