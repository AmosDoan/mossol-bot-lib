package net.mossol.bot.connection;

import net.mossol.bot.model.LinePushRequest;
import net.mossol.bot.model.LineReplyRequest;

public interface RetrofitConnection {
    void sendReply(LineReplyRequest request);

    void leaveRoom(LineReplyRequest request, String roomId);

    void leaveGroup(LineReplyRequest request, String groupId);

    void sendPush(LinePushRequest request);
}
