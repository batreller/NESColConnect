package nescol.connect.service;

import nescol.connect.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthenticatorService {
    private final JwtTokenProvider jwtTokenProvider;

    WebSocketAuthenticatorService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // This method MUST return a UsernamePasswordAuthenticationToken instance, the spring security chain is testing it with 'instanceof' later on. So don't use a subclass of it or any other class
    public UsernamePasswordAuthenticationToken getAuthenticatedOrFail(final String token) throws AuthenticationException {
        Authentication auth = null;
        if (token != null && jwtTokenProvider.validateToken(token)) {
            auth = jwtTokenProvider.getAuthentication(token);

            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        return (UsernamePasswordAuthenticationToken) auth;
    }
}
