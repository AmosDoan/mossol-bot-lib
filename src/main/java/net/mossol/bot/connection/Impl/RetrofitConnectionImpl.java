package net.mossol.bot.connection.Impl;

import java.util.Collections;

import net.mossol.bot.model.LineReplyRequest;
import net.mossol.bot.connection.RetrofitClient;
import net.mossol.bot.connection.RetrofitConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@PropertySource("classpath:connection.properties")
public class RetrofitConnectionImpl implements RetrofitConnection {
    private static final Logger logger = LoggerFactory.getLogger(RetrofitConnectionImpl.class);

    @Value("${line.token}")
    private String token;

    @Resource
    private RetrofitClient retrofitClient;

    public void sendReply(LineReplyRequest request) {
        logger.info("sendReply : requeste <{}>,token<{}>", request, token);
        retrofitClient.sendReply("Bearer " + token, request).whenComplete(
                (response , e) -> {
                    if (e != null) {
                        logger.warn("Got exception from LINE Bot Server! response<{}>", response, e);
                    }

                    if (response.code() == 200) {
                        logger.warn("Got success LINE Bot Server! request<{}>,response<{}>", request, response);
                    } else {
                        logger.warn("Got fail LINE Bot Server! request<{}>,response<{}>", request, response);
                    }
                }
        );
    }

    public void leaveRoom(LineReplyRequest request, String groupId) {
        retrofitClient.leaveRoom(groupId, "Bearer " + token, Collections.emptyMap()).whenComplete(
                (response , e) -> {
                    if (e != null) {
                        logger.warn("Got exception from LINE Bot Server! groupId<{}>,response<{}>", groupId, response, e);
                    }

                    if (response.code() == 200) {
                        logger.warn("Got success LINE Bot Server! groupId<{}>,request<{}>,response<{}>", groupId, request,
                                response);
                    } else {
                        logger.warn("Got fail LINE Bot Server! groupId<{}>,request<{}>,response<{}>", groupId, request,
                                response);
                    }
                }
        );
    }
}
