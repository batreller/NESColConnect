package nescol.connect.listener;

import nescol.connect.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Component
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {
    @Autowired
    private ChatService chatService;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        chatService.userDisconnectedFromWebsocket(event);
    }
}
