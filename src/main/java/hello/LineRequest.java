package hello;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 3..
 */
@Data
public class LineRequest {
    private List<Event> events = new ArrayList<>();

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
    }

    @Data
    public static class Message {
        private String id;
        private String type;
        private String text;
    }
}
