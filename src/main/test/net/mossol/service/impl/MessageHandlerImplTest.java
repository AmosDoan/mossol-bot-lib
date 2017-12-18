package net.mossol.service.impl;

import net.mossol.HttpConnection;
import net.mossol.model.LineRequest;
import net.mossol.service.LunchServiceHandler;
import net.mossol.service.MessageHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amos.Doan.Mac on 2017. 12. 10..
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MessageHandlerImplTest {

    @Autowired
    private MessageHandler messageHandler;

    @Mock
    private HttpConnection httpConnection;

    @Mock
    private LunchServiceHandler lunchServiceHandler;

    @Test
    public void testMessage() throws Exception {
        LineRequest lineRequest = new LineRequest();
        LineRequest.Event event = new LineRequest.Event();
        LineRequest.Message message = new LineRequest.Message();

        List<LineRequest.Event> eventList = new ArrayList<>();
        eventList.add(event);
        lineRequest.setEvents(eventList);

        message.setText("수안님께 인사");

        event.setType("message");
        event.setMessage(message);

        messageHandler.replyMessage(lineRequest);
    }
}