package net.mossol.bot.connection.Impl;

import java.util.Collections;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.mossol.bot.connection.RetrofitClient;
import net.mossol.bot.connection.RetrofitConnection;
import net.mossol.bot.model.LinePushRequest;
import net.mossol.bot.model.LineReplyRequest;

@Component
public class RetrofitConnectionImpl implements RetrofitConnection {
    private static final Logger logger = LoggerFactory.getLogger(RetrofitConnectionImpl.class);

    @Value("${line.token}")
    private String token;

    @Resource
    private RetrofitClient retrofitClient;

    @Override
    public void sendReply(LineReplyRequest request) {
        logger.info("sendReply : request <{}>,token<{}>", request, token);
        retrofitClient.sendReply("Bearer " + token, request).whenComplete(
                (response , e) -> {
                    if (e != null) {
                        logger.warn("Got exception from LINE Bot Server! response<{}>", response, e);
                        return;
                    }

                    if (response.code() == 200) {
                        logger.warn("Got success from LINE Bot Server! request<{}>,response<{}>",
                                    request, response);
                    } else {
                        logger.warn("Got failure from LINE Bot Server! request<{}>,response<{}>",
                                    request, response);
                    }
                }
        );
    }

    @Override
    public void leaveRoom(LineReplyRequest request, String roomId) {
        retrofitClient.leaveRoom(roomId, "Bearer " + token, Collections.emptyMap()).whenComplete(
                (response , e) -> {
                    if (e != null) {
                        logger.warn("Got exception from LINE Bot Server! groupId<{}>,response<{}>",
                                    roomId, response, e);
                    }

                    if (response.code() == 200) {
                        logger.warn("Got success from LINE Bot Server! groupId<{}>,request<{}>,response<{}>",
                                    roomId, request, response);
                    } else {
                        logger.warn("Got failure from LINE Bot Server! groupId<{}>,request<{}>,response<{}>",
                                    roomId, request, response);
                    }
                }
        );
    }

    @Override
    public void leaveGroup(LineReplyRequest request, String groupId) {
        retrofitClient.leaveGroup(groupId, "Bearer " + token, Collections.emptyMap()).whenComplete(
                (response , e) -> {
                    if (e != null) {
                        logger.warn("Got exception from LINE Bot Server! groupId<{}>,response<{}>",
                                    groupId, response, e);
                    }

                    if (response.code() == 200) {
                        logger.warn("Got success from LINE Bot Server! groupId<{}>,request<{}>,response<{}>",
                                    groupId, request, response);
                    } else {
                        logger.warn("Got failure from LINE Bot Server! groupId<{}>,request<{}>,response<{}>",
                                    groupId, request, response);
                    }
                }
        );
    }

    @Override
    public void sendPush(LinePushRequest request) {
        logger.info("sendPush : request <{}>,token<{}>", request, token);
        retrofitClient.sendPush("Bearer " + token, request).whenComplete(
                (response , e) -> {
                    if (e != null) {
                        logger.warn("Push request got exception from LINE Bot Server! response<{}>",
                                    response, e);
                        return;
                    }

                    if (response.code() == 200) {
                        logger.warn("Push request got success from LINE Bot Server! request<{}>,response<{}>",
                                    request, response);
                    } else {
                        logger.warn("Push request got failure from LINE Bot Server! request<{}>,response<{}>",
                                    request, response);
                    }
                }
        );
    }
}
