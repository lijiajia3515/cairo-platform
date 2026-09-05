package io.github.lijiajia3515.cairo.auth.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountPasswordAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountSnsCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountVerifyCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserAccountAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserAccountSnsCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserPasswordAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserVerifyCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserAccountAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserPasswordAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserVerifyCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.CairoOAuth2Properties;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.CairoOAuthClientFailureAuthenticationHandler;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.CairoOAuthTokenFailureHandler;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.CommonAuthorizationCodeTokenResponseClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.OAuth2AccessTokenResponseHttpMessageConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.MongodbOAuth2AuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.OAuth2AuthorizationMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.OAuth2AuthorizationMongodbMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationMongodbMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.MongodbAccountAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user.AppUserAuthorizationMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user.AppUserAuthorizationMongodbMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user.AppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user.MongodbAppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountPasswordAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountPasswordAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountRefreshTokenAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountRefreshTokenAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountSnsCodeAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountSnsCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountVerifyCodeAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountVerifyCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserAccountAccessTokenAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserAccountAccessTokenAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserAccountSnsCodeAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserAccountSnsCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserPasswordAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserPasswordAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserRefreshTokenAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserRefreshTokenAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserVerifyCodeAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserVerifyCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserAccountAccessTokenAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserAccountAccessTokenAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserPasswordAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserPasswordAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserRefreshTokenAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserRefreshTokenAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserVerifyCodeAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserVerifyCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.MongodbTenantAppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationMongodbMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.AccessTokenJwtGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.AccountAccessTokenGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.AccountAccessTokenJwtGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.AccountRefreshTokenGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.AppUserAccessTokenGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.AppUserAccessTokenJwtGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.AppUserRefreshTokenGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.ClientAccessTokenJwtGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.IdTokenJwtGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.TenantAppUserAccessTokenGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.TenantAppUserAccessTokenJwtGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.TenantAppUserRefreshTokenGenerator;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.jwt.jose.Jwks;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoBearerTokenAccessDeniedHandler;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoBearerTokenAuthenticationEntryPoint;
import io.github.lijiajia3515.cairo.auth.framework.security.web.authentication.CairoSimpleUrlAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class OAuth2ServerConfig extends OAuth2AuthorizationServerConfiguration {


	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(
		DaoAuthenticationProvider passwordAuthenticationProvider,
		CairoAccountPasswordAuthenticationProvider cairoAccountPasswordAuthenticationProvider,
		CairoAccountVerifyCodeAuthenticationProvider cairoAccountVerifyCodeAuthenticationProvider,
		CairoAccountSnsCodeAuthenticationProvider cairoAccountSnsCodeAuthenticationProvider,

		CairoAppUserPasswordAuthenticationProvider cairoAppUserPasswordAuthenticationProvider,
		CairoAppUserVerifyCodeAuthenticationProvider cairoAppUserVerifyCodeAuthenticationProvider,
		CairoAppUserAccountSnsCodeAuthenticationProvider cairoAppUserAccountSnsCodeAuthenticationProvider,
		CairoAppUserAccountAuthenticationProvider cairoAppUserAccountAuthenticationProvider,

		CairoTenantAppUserPasswordAuthenticationProvider cairoTenantAppUserPasswordAuthenticationProvider,
		CairoTenantAppUserVerifyCodeAuthenticationProvider cairoTenantAppUserVerifyCodeAuthenticationProvider,
		CairoTenantAppUserAccountAuthenticationProvider cairoTenantAppUserAccountAuthenticationProvider,

		JwtDecoder jwtDecoder,
		OAuth2TokenGenerator<OAuth2Token> oAuth2TokenGenerator,
		@Qualifier("oAuth2AuthorizationService") OAuth2AuthorizationService oAuth2AuthorizationService,
		@Qualifier("accountAuthorizationService") AccountAuthorizationService accountAuthorizationService,
		@Qualifier("appUserAuthorizationService") AppUserAuthorizationService appUserAuthorizationService,
		@Qualifier("tenantAppUserAuthorizationService") TenantAppUserAuthorizationService tenantAppUserAuthorizationService,
		CairoAuthAccountService cairoAuthAccountService,
		CairoBearerTokenAuthenticationEntryPoint entryPoint,
		CairoBearerTokenAccessDeniedHandler accessDeniedHandler,
		CairoOAuthTokenFailureHandler cairoOAuthTokenFailureHandler,
		CairoOAuthClientFailureAuthenticationHandler cairoOAuthClientFailureAuthenticationHandler,
		HttpSecurity http) throws Exception {

		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
		RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
		OrRequestMatcher requestMatcher = new OrRequestMatcher(endpointsMatcher,
			PathPatternRequestMatcher.withDefaults().matcher("/"),
			PathPatternRequestMatcher.withDefaults().matcher("/logout"),
			PathPatternRequestMatcher.withDefaults().matcher("/login"),
			PathPatternRequestMatcher.withDefaults().matcher("/login/**"),
			PathPatternRequestMatcher.withDefaults().matcher("/api/**"),
			PathPatternRequestMatcher.withDefaults().matcher("/view/**"),
			PathPatternRequestMatcher.withDefaults().matcher("/userinfo")
		);

		http
			.securityMatcher(requestMatcher)
			.authorizeHttpRequests(config ->
				config.anyRequest().permitAll()
			)
			.cors(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.logout(config ->
				config.logoutSuccessUrl("/")
					.permitAll())
			.formLogin(config -> config.loginPage("/login")
				.permitAll()
				.successHandler(new SavedRequestAwareAuthenticationSuccessHandler())
				.failureHandler(new CairoSimpleUrlAuthenticationFailureHandler("/login?error"))
			)
			.oauth2Login(x ->
					x.loginPage("/login")
						.permitAll()
						.tokenEndpoint(tokenEndpointConfig ->
							tokenEndpointConfig.accessTokenResponseClient(new CommonAuthorizationCodeTokenResponseClient()))
			)
			.authenticationProvider(passwordAuthenticationProvider)
			.authenticationProvider(cairoAccountPasswordAuthenticationProvider)
			.authenticationProvider(cairoAccountVerifyCodeAuthenticationProvider)
			.authenticationProvider(cairoAccountSnsCodeAuthenticationProvider)

			.authenticationProvider(cairoAppUserPasswordAuthenticationProvider)
			.authenticationProvider(cairoAppUserVerifyCodeAuthenticationProvider)
			.authenticationProvider(cairoAppUserAccountSnsCodeAuthenticationProvider)
			.authenticationProvider(cairoAppUserAccountAuthenticationProvider)

			.authenticationProvider(cairoTenantAppUserPasswordAuthenticationProvider)
			.authenticationProvider(cairoTenantAppUserVerifyCodeAuthenticationProvider)
			.authenticationProvider(cairoTenantAppUserAccountAuthenticationProvider)

			.sessionManagement(config -> {
				config.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
			})

			.rememberMe(config ->
				config.userDetailsService(cairoAuthAccountService))

			.exceptionHandling(config -> {
				config.authenticationEntryPoint(entryPoint)
					.accessDeniedHandler(accessDeniedHandler);
			});

		authorizationServerConfigurer
			.clientAuthentication(config ->
				config.errorResponseHandler(cairoOAuthClientFailureAuthenticationHandler))
			.tokenEndpoint(config -> {
				createDefaultAuthenticationProviders(
					passwordAuthenticationProvider,
					cairoAccountPasswordAuthenticationProvider,
					cairoAccountVerifyCodeAuthenticationProvider,
					cairoAccountSnsCodeAuthenticationProvider,

					cairoAppUserPasswordAuthenticationProvider,
					cairoAppUserVerifyCodeAuthenticationProvider,
					cairoAppUserAccountSnsCodeAuthenticationProvider,
					cairoAppUserAccountAuthenticationProvider,

					cairoTenantAppUserPasswordAuthenticationProvider,
					cairoTenantAppUserVerifyCodeAuthenticationProvider,
					cairoTenantAppUserAccountAuthenticationProvider,

					oAuth2AuthorizationService,
					accountAuthorizationService,
					appUserAuthorizationService,
					tenantAppUserAuthorizationService,
					oAuth2TokenGenerator,
					jwtDecoder)
					.forEach(config::authenticationProvider);

				config.accessTokenRequestConverter(oauth2TokenConverter())
					.errorResponseHandler(cairoOAuthTokenFailureHandler)
				;
			})
			.oidc(config -> {

			})
		;

		http.with(authorizationServerConfigurer, config -> {

		});
		return http.build();
	}

	@Primary
	@Bean("oAuth2AuthorizationService")
	OAuth2AuthorizationService oAuth2AuthorizationService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
														  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
														  RegisteredClientRepository registeredClientRepository) {
		OAuth2AuthorizationMongodbMapper oAuth2AuthorizationMongodbMapper = new OAuth2AuthorizationMongodbMapper();
		OAuth2AuthorizationMapper oAuth2AuthorizationMapper = new OAuth2AuthorizationMapper(registeredClientRepository);
		return new MongodbOAuth2AuthorizationService(mongoTemplate, readMongoTemplate, oAuth2AuthorizationMapper, oAuth2AuthorizationMongodbMapper);
	}

	@Bean
	AccountAuthorizationService accountAuthorizationService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
															TransactionTemplate transactionTemplate,
															@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
															RedisTemplate<String, Object> redisTemplate,
															RegisteredClientRepository registeredClientRepository) {
		AccountAuthorizationMongodbMapper accountAuthorizationMongodbMapper = new AccountAuthorizationMongodbMapper(registeredClientRepository);
		AccountAuthorizationMapper accountAuthorizationMapper = new AccountAuthorizationMapper(registeredClientRepository);
		return new MongodbAccountAuthorizationService(mongoTemplate, transactionTemplate, readMongoTemplate, redisTemplate, accountAuthorizationMapper, accountAuthorizationMongodbMapper);
	}

	@Bean
	MongodbAppUserAuthorizationService appUserAuthorizationService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																				   TransactionTemplate transactionTemplate,
																				   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
																				   RedisTemplate<String, Object> redisTemplate,
																				   RegisteredClientRepository registeredClientRepository) {
		AppUserAuthorizationMongodbMapper authorizationMongodbMapper = new AppUserAuthorizationMongodbMapper();
		AppUserAuthorizationMapper authorizationMapper = new AppUserAuthorizationMapper(registeredClientRepository);
		return new MongodbAppUserAuthorizationService(mongoTemplate, transactionTemplate, readMongoTemplate, redisTemplate, authorizationMapper, authorizationMongodbMapper);
	}

	@Bean
	MongodbTenantAppUserAuthorizationService tenantAppUserAuthorizationService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																							   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
																							   RedisTemplate<String, Object> redisTemplate,
																							   RegisteredClientRepository registeredClientRepository) {
		TenantAppUserAuthorizationMongodbMapper authorizationMongodbMapper = new TenantAppUserAuthorizationMongodbMapper();
		TenantAppUserAuthorizationMapper authorizationMapper = new TenantAppUserAuthorizationMapper(registeredClientRepository);
		return new MongodbTenantAppUserAuthorizationService(mongoTemplate, readMongoTemplate, redisTemplate, authorizationMapper, authorizationMongodbMapper);
	}

	@Bean
	OAuth2TokenGenerator<OAuth2Token> cairoOAuth2TokenGenerator(JWKSource<SecurityContext> jwkSource) {
		NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);

		IdTokenJwtGenerator idTokenJwtGenerator = new IdTokenJwtGenerator(jwtEncoder);
		AccessTokenJwtGenerator accessTokenJwtGenerator = new AccessTokenJwtGenerator(jwtEncoder);
		ClientAccessTokenJwtGenerator clientAccessTokenJwtGenerator = new ClientAccessTokenJwtGenerator(jwtEncoder);

		AccountAccessTokenJwtGenerator accountAccessTokenJwtGenerator = new AccountAccessTokenJwtGenerator(jwtEncoder);
		AppUserAccessTokenJwtGenerator appUserAccessTokenJwtGenerator = new AppUserAccessTokenJwtGenerator(jwtEncoder);
		TenantAppUserAccessTokenJwtGenerator tenantAppUserAccessTokenJwtGenerator = new TenantAppUserAccessTokenJwtGenerator(jwtEncoder);

		OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
		OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

		AccountAccessTokenGenerator accountAccessTokenGenerator = new AccountAccessTokenGenerator();
		AccountRefreshTokenGenerator accountRefreshTokenGenerator = new AccountRefreshTokenGenerator();

		AppUserAccessTokenGenerator appUserAccessTokenGenerator = new AppUserAccessTokenGenerator();
		AppUserRefreshTokenGenerator appUserRefreshTokenGenerator = new AppUserRefreshTokenGenerator();

		TenantAppUserAccessTokenGenerator tenantAppUserAccessTokenGenerator = new TenantAppUserAccessTokenGenerator();
		TenantAppUserRefreshTokenGenerator tenantAppUserRefreshTokenGenerator = new TenantAppUserRefreshTokenGenerator();

		return new DelegatingOAuth2TokenGenerator(
			clientAccessTokenJwtGenerator,
			idTokenJwtGenerator,
			accessTokenJwtGenerator,
			accessTokenGenerator,
			refreshTokenGenerator,
			accountAccessTokenJwtGenerator,
			accountAccessTokenGenerator,
			accountRefreshTokenGenerator,
			appUserAccessTokenJwtGenerator,
			appUserAccessTokenGenerator,
			appUserRefreshTokenGenerator,
			tenantAppUserAccessTokenJwtGenerator,
			tenantAppUserAccessTokenGenerator,
			tenantAppUserRefreshTokenGenerator
		);
	}

	@Bean
	public AuthorizationServerSettings providerSettings(CairoOAuth2Properties properties) {
		return AuthorizationServerSettings.builder()
			.issuer(properties.getIssuer())
			.build();
	}

	@Bean
	public HttpMessageConverter<OAuth2AccessTokenResponse> accessTokenResponseHttpMessageConverter(ObjectMapper objectMapper) {
		return new OAuth2AccessTokenResponseHttpMessageConverter(objectMapper);
	}

	@Bean
	public CairoOAuthTokenFailureHandler cairoOAuth2TokenFailureHandler(HttpMessageConverters messageConverters) {
		return new CairoOAuthTokenFailureHandler(messageConverters.getConverters());
	}

	@Bean
	public CairoOAuthClientFailureAuthenticationHandler cairoOAuth2ClientFailureHandler(HttpMessageConverters messageConverters) {
		return new CairoOAuthClientFailureAuthenticationHandler(messageConverters.getConverters());
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource(CairoOAuth2Properties properties) {
		List<JWK> jwks = properties.getRsaKeys().stream().map(x -> {
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(x.getPublicKey().getBytes()));
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(x.getPrivateKey().getBytes()));
			try {
				KeyFactory instance = KeyFactory.getInstance("RSA");
				RSAPublicKey publicKey = (RSAPublicKey) instance.generatePublic(publicKeySpec);
				RSAPrivateKey privateKey = (RSAPrivateKey) instance.generatePrivate(privateKeySpec);
				return (JWK) new RSAKey.Builder(publicKey).keyID(x.getId()).privateKey(privateKey).build();
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				return Jwks.generateRsa(x.getId());
			}
		}).collect(Collectors.toList());

		JWKSet jwkSet = new JWKSet(jwks);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	AuthenticationConverter oauth2TokenConverter() {
		return new DelegatingAuthenticationConverter(
			Arrays.asList(
				// oauth2原始模式
				new OAuth2ClientCredentialsAuthenticationConverter(),
				new OAuth2AuthorizationCodeAuthenticationConverter(),
				new OAuth2RefreshTokenAuthenticationConverter(),

				// 账号
				new OAuthAccountPasswordAuthenticationConverter(),
				new OAuthAccountVerifyCodeAuthenticationConverter(),
				new OAuthAccountSnsCodeAuthenticationConverter(),
				new OAuthAccountRefreshTokenAuthenticationConverter(),

				// 应用级用户
				new OAuthAppUserPasswordAuthenticationConverter(),
				new OAuthAppUserVerifyCodeAuthenticationConverter(),
				new OAuthAppUserAccountSnsCodeAuthenticationConverter(),
				new OAuthAppUserAccountAccessTokenAuthenticationConverter(),
				new OAuthAppUserRefreshTokenAuthenticationConverter(),

				// 企业应用级用户
				new OAuthTenantAppUserPasswordAuthenticationConverter(),
				new OAuthTenantAppUserVerifyCodeAuthenticationConverter(),
				new OAuthTenantAppUserAccountAccessTokenAuthenticationConverter(),
				new OAuthTenantAppUserRefreshTokenAuthenticationConverter()
			)
		);
	}

	<B extends HttpSecurityBuilder<B>> List<AuthenticationProvider> createDefaultAuthenticationProviders(DaoAuthenticationProvider passwordAuthenticationProvider,

																										 CairoAccountPasswordAuthenticationProvider cairoAccountPasswordAuthenticationProvider,
																										 CairoAccountVerifyCodeAuthenticationProvider cairoAccountVerifyCodeAuthenticationProvider,
																										 CairoAccountSnsCodeAuthenticationProvider cairoAccountSnsCodeAuthenticationProvider,

																										 CairoAppUserPasswordAuthenticationProvider cairoAppUserPasswordAuthenticationProvider,
																										 CairoAppUserVerifyCodeAuthenticationProvider cairoAppUserVerifyCodeAuthenticationProvider,
																										 CairoAppUserAccountSnsCodeAuthenticationProvider cairoAppUserAccountSnsCodeAuthenticationProvider,
																										 CairoAppUserAccountAuthenticationProvider cairoAppUserAccountAuthenticationProvider,

																										 CairoTenantAppUserPasswordAuthenticationProvider cairoTenantAppUserPasswordAuthenticationProvider,
																										 CairoTenantAppUserVerifyCodeAuthenticationProvider cairoTenantAppUserVerifyCodeAuthenticationProvider,
																										 CairoTenantAppUserAccountAuthenticationProvider cairoTenantAppUserAccountAuthenticationProvider,

																										 OAuth2AuthorizationService oauth2AuthorizationService,
																										 AccountAuthorizationService accountAuthorizationService,
																										 AppUserAuthorizationService appUserAuthorizationService,
																										 TenantAppUserAuthorizationService tenantAppUserAuthorizationService,
																										 OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
																										 JwtDecoder jwtDecoder) {
		List<AuthenticationProvider> authenticationProviders = new ArrayList<>();

		ProviderManager providerManager = new ProviderManager(
			passwordAuthenticationProvider,
			cairoAccountPasswordAuthenticationProvider,
			cairoAccountVerifyCodeAuthenticationProvider,
			cairoAccountSnsCodeAuthenticationProvider,

			cairoAppUserPasswordAuthenticationProvider,
			cairoAppUserVerifyCodeAuthenticationProvider,
			cairoAppUserAccountSnsCodeAuthenticationProvider,
			cairoAppUserAccountAuthenticationProvider,

			cairoTenantAppUserPasswordAuthenticationProvider,
			cairoTenantAppUserVerifyCodeAuthenticationProvider,
			cairoTenantAppUserAccountAuthenticationProvider
		);

		providerManager.setEraseCredentialsAfterAuthentication(true);
		providerManager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher());

		OAuth2ClientCredentialsAuthenticationProvider clientCredentialsAuthenticationProvider = new OAuth2ClientCredentialsAuthenticationProvider(oauth2AuthorizationService, tokenGenerator);
		authenticationProviders.add(clientCredentialsAuthenticationProvider);

		OAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider = new OAuth2AuthorizationCodeAuthenticationProvider(oauth2AuthorizationService, tokenGenerator);
		authenticationProviders.add(authorizationCodeAuthenticationProvider);

		OAuth2RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider = new OAuth2RefreshTokenAuthenticationProvider(oauth2AuthorizationService, tokenGenerator);
		authenticationProviders.add(refreshTokenAuthenticationProvider);

		// account
		OAuthAccountPasswordAuthenticationProvider oauthAccountPasswordAuthenticationProvider = new OAuthAccountPasswordAuthenticationProvider(providerManager, accountAuthorizationService, tokenGenerator);
		authenticationProviders.add(oauthAccountPasswordAuthenticationProvider);

		OAuthAccountVerifyCodeAuthenticationProvider oAuthAccountVerifyCodeAuthenticationProvider = new OAuthAccountVerifyCodeAuthenticationProvider(providerManager, accountAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthAccountVerifyCodeAuthenticationProvider);

		OAuthAccountSnsCodeAuthenticationProvider oAuthAccountSnsCodeAuthenticationProvider = new OAuthAccountSnsCodeAuthenticationProvider(providerManager, accountAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthAccountSnsCodeAuthenticationProvider);

		OAuthAccountRefreshTokenAuthenticationProvider oAuthAccountRefreshTokenAuthenticationProvider = new OAuthAccountRefreshTokenAuthenticationProvider(accountAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthAccountRefreshTokenAuthenticationProvider);

		// app user
		OAuthAppUserPasswordAuthenticationProvider oAuthAppUserPasswordAuthenticationProvider = new OAuthAppUserPasswordAuthenticationProvider(providerManager, appUserAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthAppUserPasswordAuthenticationProvider);

		OAuthAppUserVerifyCodeAuthenticationProvider oAuthAppUserVerifyCodeAuthenticationProvider = new OAuthAppUserVerifyCodeAuthenticationProvider(providerManager, appUserAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthAppUserVerifyCodeAuthenticationProvider);

		OAuthAppUserAccountSnsCodeAuthenticationProvider oAuthAppUserAccountSnsCodeAuthenticationProvider = new OAuthAppUserAccountSnsCodeAuthenticationProvider(providerManager, appUserAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthAppUserAccountSnsCodeAuthenticationProvider);

		OAuthAppUserAccountAccessTokenAuthenticationProvider oAuthAppUserAccountAccessTokenAuthenticationProvider = new OAuthAppUserAccountAccessTokenAuthenticationProvider(providerManager, accountAuthorizationService, appUserAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthAppUserAccountAccessTokenAuthenticationProvider);

		OAuthAppUserRefreshTokenAuthenticationProvider oAuthAppUserRefreshTokenAuthenticationProvider = new OAuthAppUserRefreshTokenAuthenticationProvider(appUserAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthAppUserRefreshTokenAuthenticationProvider);

		// tenant app user
		OAuthTenantAppUserPasswordAuthenticationProvider oAuthTenantAppUserPasswordAuthenticationProvider = new OAuthTenantAppUserPasswordAuthenticationProvider(providerManager, tenantAppUserAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthTenantAppUserPasswordAuthenticationProvider);

		OAuthTenantAppUserVerifyCodeAuthenticationProvider oAuthTenantAppUserVerifyCodeAuthenticationProvider = new OAuthTenantAppUserVerifyCodeAuthenticationProvider(providerManager, tenantAppUserAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthTenantAppUserVerifyCodeAuthenticationProvider);

		OAuthTenantAppUserAccountAccessTokenAuthenticationProvider oAuthTenantAppUserAccountAccessTokenAuthenticationProvider = new OAuthTenantAppUserAccountAccessTokenAuthenticationProvider(providerManager, accountAuthorizationService, tenantAppUserAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthTenantAppUserAccountAccessTokenAuthenticationProvider);

		OAuthTenantAppUserRefreshTokenAuthenticationProvider oAuthTenantAppUserRefreshTokenAuthenticationProvider = new OAuthTenantAppUserRefreshTokenAuthenticationProvider(tenantAppUserAuthorizationService, tokenGenerator);
		authenticationProviders.add(oAuthTenantAppUserRefreshTokenAuthenticationProvider);

		return authenticationProviders;
	}


}
