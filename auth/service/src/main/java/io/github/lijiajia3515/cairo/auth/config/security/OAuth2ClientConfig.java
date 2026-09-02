package io.github.lijiajia3515.cairo.auth.config.security;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.http.CairoOAuth2ResponseErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration(proxyBeanMethods = false)
public class OAuth2ClientConfig {
    @Bean
    public RestClientClientCredentialsTokenResponseClient cairoClientCredentialsTokenResponseClient() {
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        CairoOAuth2ResponseErrorHandler cairoOAuth2ResponseErrorHandler = new CairoOAuth2ResponseErrorHandler();
        restTemplate.setErrorHandler(cairoOAuth2ResponseErrorHandler);
        RestClient restClient = RestClient.builder(restTemplate).build();
        RestClientClientCredentialsTokenResponseClient client = new RestClientClientCredentialsTokenResponseClient();
        client.setRestClient(restClient);
        return client;
    }

    @Bean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager cairoOAuth2AuthorizedClientManager(RestClientClientCredentialsTokenResponseClient client, ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(clientCredentialsGrantBuilder -> clientCredentialsGrantBuilder.accessTokenResponseClient(client))
                .build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }
}
