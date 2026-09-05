package io.github.lijiajia3515.cairo.auth.config.security;

import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.CairoOAuth2Properties;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user.AppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication.CairoOAuthJwtAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoBearerTokenAccessDeniedHandler;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoBearerTokenAuthenticationEntryPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class OAuth2ClientServerConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
											CairoBearerTokenAuthenticationEntryPoint entryPoint,
											CairoBearerTokenAccessDeniedHandler accessDeniedHandler,
											Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter) throws Exception {
		OrRequestMatcher requestMatcher = new OrRequestMatcher(
			PathPatternRequestMatcher.withDefaults().matcher("/client_api/**")
		);
		http
			.securityMatcher(requestMatcher)
			.cors(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(config-> {
				config.anyRequest().permitAll();
			})
			.oauth2ResourceServer(config -> {
				config
					.authenticationEntryPoint(entryPoint)
					.accessDeniedHandler(accessDeniedHandler)
					.jwt(jwtConfig -> {
						jwtConfig
							.jwtAuthenticationConverter(jwtAuthenticationConverter);
					})
				;
			})
			.sessionManagement(config -> {
				config.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
			})
		;

		return http.build();
	}

	@Bean
	public CairoBearerTokenAuthenticationEntryPoint cairoBearerTokenAuthenticationEntryPoint(HttpMessageConverters messageConverters) {
		return new CairoBearerTokenAuthenticationEntryPoint(messageConverters.getConverters()).realmName("cairo");
	}

	@Bean
	public CairoBearerTokenAccessDeniedHandler cairoBearerTokenAccessDeniedHandler(HttpMessageConverters httpMessageConverters) {
		return new CairoBearerTokenAccessDeniedHandler(httpMessageConverters.getConverters()).realmName("cairo");
	}

	@Bean
	@Primary
	CairoOAuthJwtAuthenticationConverter cairoOAuthJwtAuthenticationConverter(CairoAuthAccountService cairoAuthAccountService,
                                                                              CairoAuthTenantAppUserService cairoAuthTenantAppUserService,
                                                                              CairoAuthAppUserService cairoAuthAppUserService,
                                                                              AccountAuthorizationService accountAuthorizationService,
                                                                              AppUserAuthorizationService appUserAuthorizationService,
                                                                              TenantAppUserAuthorizationService tenantAppUserAuthorizationService) {
		return new CairoOAuthJwtAuthenticationConverter(cairoAuthAccountService, cairoAuthTenantAppUserService, cairoAuthAppUserService,
			accountAuthorizationService, appUserAuthorizationService, tenantAppUserAuthorizationService);
	}

	@Bean
	public JwtDecoder cairoJwtDecoder(CairoOAuth2Properties properties) {
		return properties.getRsaKeys().stream()
			.map(x -> {
				X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(x.getPublicKey().getBytes()));
				try {
					KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					return (RSAPublicKey) keyFactory.generatePublic(spec);

				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					log.debug("", e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.findFirst()
			.map(x -> NimbusJwtDecoder.withPublicKey(x).build())
			.orElseThrow(() -> new RuntimeException("无法配置 jwt 公钥解析器"));
	}
}
