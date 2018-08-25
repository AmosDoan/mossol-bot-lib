package net.mossol.connection.Impl;

import net.mossol.connection.RetrofitClient;
import net.mossol.connection.RetrofitConnection;
import net.mossol.model.LineReplyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@PropertySource("classpath:connection.properties")
public class RetrofitConnectionImpl implements RetrofitConnection {
    private final static Logger logger = LoggerFactory.getLogger(RetrofitConnectionImpl.class);

    @Value("${retrofit.token}")
    private String token;

    @Resource
    private RetrofitClient retrofitClient;

    public void sendReply(LineReplyRequest request) {
        logger.info("sendReply : requeste <{}>,token<{}>", request, token);
        retrofitClient.sendReply("Bearer " + token, request).whenComplete(
                (response , e) -> {
                    if (e != null) {
                        logger.warn("Got exception from LINE Bot Server! reponse<{}>", response, e);
                    }

                    if (response.code() == 200) {
                        logger.warn("Got success LINE Bot Server! request<{}>,reponse<{}>", request, response);
                    } else {
                        logger.warn("Got fail LINE Bot Server! request<{}>,reponse<{}>", request, response);
                    }
                }
        );
    }

    public void leaveRoom(LineReplyRequest request, String groupId) {
        retrofitClient.leaveRoom(groupId, "Bearer " + token, request).whenComplete(
                (response , e) -> {
                    if (e != null) {
                        logger.warn("Got exception from LINE Bot Server! groupId<{}>,reponse<{}>", groupId, response, e);
                    }

                    if (response.code() == 200) {
                        logger.warn("Got success LINE Bot Server! groupId<{}>,request<{}>,reponse<{}>", groupId, request,
                                response);
                    } else {
                        logger.warn("Got fail LINE Bot Server! groupId<{}>,request<{}>,reponse<{}>", groupId, request,
                                response);
                    }
                }
        );
    }
}
