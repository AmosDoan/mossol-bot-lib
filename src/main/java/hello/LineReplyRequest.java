package hello;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 3..
 */
@Data
public class LineReplyRequest {
    private String replyToken;
    private List<Message> messages = new ArrayList<>();

    public void setMessage(Message message) {
        messages.add(message);
    }

    @Data
    public static class Message {
        private String type;
        private String text;
    }
}
