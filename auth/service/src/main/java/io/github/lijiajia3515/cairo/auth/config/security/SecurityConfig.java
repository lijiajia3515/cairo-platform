package io.github.lijiajia3515.cairo.auth.config.security;

import io.github.lijiajia3515.cairo.auth.framework.security.event.CairoAuthenticationEventPublisher;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationMongodbMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.MongodbAccountAuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Slf4j
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web
                .debug(false)
                .ignoring()
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .requestMatchers("/favicon.ico")
                .requestMatchers("/static/**", "/public/**")
                .requestMatchers("/actuator/**")
                .requestMatchers("/open_api/**")
                ;
    }

    @Bean
    @Primary
    AuthenticationEventPublisher cairoAuthenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new CairoAuthenticationEventPublisher(publisher);
    }



}
