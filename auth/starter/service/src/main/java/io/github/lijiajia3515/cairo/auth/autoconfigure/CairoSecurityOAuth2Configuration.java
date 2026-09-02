package io.github.lijiajia3515.cairo.auth.autoconfigure;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.http.CairoOAuth2ResponseErrorHandler;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication.CairoRemoteAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoBearerTokenAccessDeniedHandler;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoBearerTokenAuthenticationEntryPoint;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.client.TenantAppUserClientApiService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Configuration(proxyBeanMethods = false)
public class CairoSecurityOAuth2Configuration {
	@Bean
	@ConfigurationProperties(prefix = "cairo.security.oauth2.resourceserver.jwt")
	OAuth2ResourceServerProperties.Jwt cairoOAuth2ResourceserverJwtProperties() {
		return new OAuth2ResourceServerProperties.Jwt();
	}

	@Configuration(proxyBeanMethods = false)
	public static class ResourceServerConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public BearerTokenResolver cairoBearerTokenResolver() {
			DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
			resolver.setAllowFormEncodedBodyParameter(true);
			resolver.setAllowUriQueryParameter(true);
			return resolver;
		}

		@Bean
		@ConditionalOnClass(CairoBearerTokenAuthenticationEntryPoint.class)
		@ConditionalOnMissingBean
		public CairoBearerTokenAuthenticationEntryPoint cairoBearerTokenAuthenticationEntryPoint(HttpMessageConverters messageConverters) {
			return new CairoBearerTokenAuthenticationEntryPoint(messageConverters.getConverters()).realmName("cairo");
		}

		@Bean
		@ConditionalOnClass(CairoBearerTokenAccessDeniedHandler.class)
		@ConditionalOnMissingBean
		public CairoBearerTokenAccessDeniedHandler cairoBearerTokenAccessDeniedHandler(HttpMessageConverters httpMessageConverters) {
			return new CairoBearerTokenAccessDeniedHandler(httpMessageConverters.getConverters()).realmName("cairo");
		}

		@Bean
		@ConditionalOnClass({TenantAppUserClientApiService.class, AccountClientApiService.class, CairoRemoteAuthenticationConverter.class})
		@ConditionalOnMissingBean
		public CairoRemoteAuthenticationConverter cairoJwtAuthenticationConverter(ObjectProvider<AccountClientApiService> accountClientApiServices,
																				  ObjectProvider<TenantAppUserClientApiService> tenantAppUserClientApiServices,
																				  ObjectProvider<AppUserClientApiService> appUserClientApiServices) {
			return new CairoRemoteAuthenticationConverter(accountClientApiServices.getIfAvailable(), tenantAppUserClientApiServices.getIfAvailable(),
				appUserClientApiServices.getIfAvailable());
		}
	}

	@Configuration(proxyBeanMethods = false)
	@AutoConfigureBefore(name = "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerJwtConfiguration$JwtDecoderConfiguration")
	@AutoConfigureAfter(CairoSecurityOAuth2Configuration.class)

	public static class CairoOAuth2ResourceServerJwtConfiguration {
		private final OAuth2ResourceServerProperties.Jwt properties;


		public CairoOAuth2ResourceServerJwtConfiguration(@Qualifier("cairoOAuth2ResourceserverJwtProperties") OAuth2ResourceServerProperties.Jwt oAuth2ResourceServerJwtProperties) {
			this.properties = oAuth2ResourceServerJwtProperties;
		}

		@Bean
		@ConditionalOnBean({OAuth2ResourceServerProperties.class, RestTemplate.class})
		@ConditionalOnProperty(name = "cairo.security.oauth2.resourceserver.jwt.jwk-set-uri")
		public NimbusJwtDecoder cairoJwtDecoder(RestTemplate restTemplate,
												OAuth2ResourceServerProperties properties) {
			OAuth2ResourceServerProperties.Jwt jwt = properties.getJwt();
			NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwt.getJwkSetUri())
				.jwsAlgorithms(this::jwsAlgorithms)
				.restOperations(restTemplate).build();
			String issuerUri = jwt.getIssuerUri();
			Supplier<OAuth2TokenValidator<Jwt>> defaultValidator = (issuerUri != null)
				? () -> JwtValidators.createDefaultWithIssuer(issuerUri) : JwtValidators::createDefault;
			nimbusJwtDecoder.setJwtValidator(getValidators(defaultValidator));
			return nimbusJwtDecoder;
		}

		private void jwsAlgorithms(Set<SignatureAlgorithm> signatureAlgorithms) {
			for (String algorithm : this.properties.getJwsAlgorithms()) {
				signatureAlgorithms.add(SignatureAlgorithm.from(algorithm));
			}
		}

		private OAuth2TokenValidator<Jwt> getValidators(Supplier<OAuth2TokenValidator<Jwt>> defaultValidator) {
			OAuth2TokenValidator<Jwt> defaultValidators = defaultValidator.get();
			List<String> audiences = this.properties.getAudiences();
			if (CollectionUtils.isEmpty(audiences)) {
				return defaultValidators;
			}
			List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
			validators.add(defaultValidators);
			validators.add(new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
				(aud) -> aud != null && !Collections.disjoint(aud, audiences)));
			return new DelegatingOAuth2TokenValidator<>(validators);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@AutoConfigureAfter(CairoSecurityOAuth2Configuration.class)
	public static class CairoOAuth2ClientConfiguration {

		@Bean
		public RestClientClientCredentialsTokenResponseClient cairoClientCredentialsTokenResponseClient() {
			RestTemplate restTemplate = new RestTemplate(
				Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
			restTemplate.setErrorHandler(new CairoOAuth2ResponseErrorHandler());
			RestClient restClient = RestClient.builder(restTemplate).build();
			RestClientClientCredentialsTokenResponseClient client = new RestClientClientCredentialsTokenResponseClient();
			client.setRestClient(restClient);
			return client;
		}

		@Bean
		@Order(Ordered.HIGHEST_PRECEDENCE)
		public AuthorizedClientServiceOAuth2AuthorizedClientManager cairoOAuth2AuthorizedClientManager(RestClientClientCredentialsTokenResponseClient client,
																									   ClientRegistrationRepository clientRegistrationRepository,
																									   OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
			OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder
				.builder().clientCredentials(clientCredentialsGrantBuilder ->
					clientCredentialsGrantBuilder.accessTokenResponseClient(client)
				).build();
			AuthorizedClientServiceOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService);
			manager.setAuthorizedClientProvider(provider);
			return manager;
		}
	}

}
