package net.mossol.bot.bot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 3..
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineRequest {
    private List<Event> events = new ArrayList<>();
    private String destination;

    @Data
    public static class Event {
        private String replyToken;
        private String type;
        private String timestamp;
        private Source source;
        private Message message;
    }

    @Data
    public static class Source {
        private String type;
        private String userId;
        private String groupId;
        private String roomId;
    }

    @Data
    public static class Message {
        private String id;
        private String type;
        private String text;
        private String packageId;
        private String stickerId;
    }
}
