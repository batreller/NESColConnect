package nescol.connect.config;

import nescol.connect.security.jwt.JwtConfigurer;
import nescol.connect.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import javax.annotation.Resource;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private JwtTokenProvider jwtTokenProvider;

    private static final String AUTH_ENDPOINT = "/api/auth/*";
    private static final String HEAD_ENDPOINT = "/";
    private static final String JS_ENDPOINT = "/js/**";
    private static final String CSS_ENDPOINT = "/css/**";
    private static final String FAVICON_ENDPOINT = "/favicon.ico";
    private static final String WS_ENDPOINT = "/ws/**";

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers(WS_ENDPOINT).permitAll()
                .antMatchers(HEAD_ENDPOINT).permitAll()
                .antMatchers(JS_ENDPOINT).permitAll()
                .antMatchers(CSS_ENDPOINT).permitAll()
                .antMatchers(FAVICON_ENDPOINT).permitAll()
                .antMatchers(AUTH_ENDPOINT).permitAll()
                .anyRequest().authenticated();

        // Add JWT token configuration
        http.apply(new JwtConfigurer(jwtTokenProvider));
    }

    @Resource
    public void setJwtTokenProvider(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
}
