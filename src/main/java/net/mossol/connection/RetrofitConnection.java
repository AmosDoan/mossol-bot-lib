package net.mossol.connection;

import net.mossol.model.LineReplyRequest;

public interface RetrofitConnection {
    void sendReply(LineReplyRequest request);

    void leaveRoom(LineReplyRequest request, String groupId);
}
