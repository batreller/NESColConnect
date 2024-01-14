package nescol.connect.interceptor;

import nescol.connect.service.ChatService;
import nescol.connect.service.WebSocketAuthenticatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {
    private static final String TOKEN_HEADER = "token";
    private final WebSocketAuthenticatorService webSocketAuthenticatorService;

    @Autowired
    private ChatService chatService;

    @Inject
    public AuthChannelInterceptorAdapter(final WebSocketAuthenticatorService webSocketAuthenticatorService) {
        this.webSocketAuthenticatorService = webSocketAuthenticatorService;
    }

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assert accessor != null;
        if (StompCommand.CONNECT == accessor.getCommand()) {
            String token = accessor.getFirstNativeHeader(TOKEN_HEADER);
            final UsernamePasswordAuthenticationToken user = webSocketAuthenticatorService.getAuthenticatedOrFail(token);
            chatService.saveSession(user, accessor.getSessionId());
            accessor.setUser(user);
        }
        return message;
    }
}
