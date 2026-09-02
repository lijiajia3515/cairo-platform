package io.github.lijiajia3515.cairo.auth.config.security;

import io.github.lijiajia3515.cairo.auth.framework.security.account.AccountAuthenticationConfigurer;
import io.github.lijiajia3515.cairo.auth.framework.security.account.AccountAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.account.MyAccountAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.AppUserAuthenticationConfigurer;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.AppUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.MyAppUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.subapp_user.SubappUserAuthenticationConfigurer;
import io.github.lijiajia3515.cairo.auth.framework.security.subapp_user.SubappUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.subapp_user.MySubappUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.subapp_user.CairoAuthSubappUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user.CairoAuthTenantSubappUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user.AppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.MyTenantAppUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.TenantAppUserAuthenticationConfigurer;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.TenantAppUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user.MyTenantSubappUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user.TenantSubappUserAuthenticationConfigurer;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user.TenantSubappUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoBearerTokenAccessDeniedHandler;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoBearerTokenAuthenticationEntryPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class OAuth2WebServerConfig {

	@Bean
	SecurityFilterChain webServerSecurityFilterChain(HttpSecurity http,
													 ApplicationContext applicationContext,
													 CairoBearerTokenAuthenticationEntryPoint entryPoint,
													 CairoBearerTokenAccessDeniedHandler accessDeniedHandler,
													 Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter) throws Exception {
//		OrRequestMatcher requestMatcher = new OrRequestMatcher(
//			new AntPathRequestMatcher("/account_api/**"),
//			new AntPathRequestMatcher("/app_user_api/**"),
//			new AntPathRequestMatcher("/tenant_app_user_api/**"),
//		);
		http
//			.securityMatcher(requestMatcher)
			.cors(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(config -> {
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

		http.with(new AccountAuthenticationConfigurer(applicationContext), config -> {
			config.setAuthenticationEntryPoint(entryPoint);
		});

		http.with(new AppUserAuthenticationConfigurer(applicationContext), config -> {
			config.setAuthenticationEntryPoint(entryPoint);
		});

		http.with(new SubappUserAuthenticationConfigurer(applicationContext), config -> {
			config.setAuthenticationEntryPoint(entryPoint);
		});

		http.with(new TenantAppUserAuthenticationConfigurer(applicationContext), config -> {
			config.setAuthenticationEntryPoint(entryPoint);
		});

		http.with(new TenantSubappUserAuthenticationConfigurer(applicationContext), config -> {
			config.setAuthenticationEntryPoint(entryPoint);
		});

		return http.build();
	}

	@Bean
	AccountAuthenticationTokenConverter accountAuthenticationTokenConverter(CairoAuthAccountService cairoAuthAccountService, AccountAuthorizationService accountAuthorizationService) {
		return new MyAccountAuthenticationTokenConverter(cairoAuthAccountService, accountAuthorizationService);
	}

	@Bean
	AppUserAuthenticationTokenConverter appUserAuthenticationTokenConverter(CairoAuthAppUserService cairoAuthAppUserService, AppUserAuthorizationService appUserAuthorizationService) {
		return new MyAppUserAuthenticationTokenConverter(cairoAuthAppUserService, appUserAuthorizationService);
	}

	@Bean
	SubappUserAuthenticationTokenConverter subappUserAuthenticationTokenConverter(CairoAuthSubappUserService cairoAuthAppUserService, AppUserAuthorizationService appUserAuthorizationService) {
		return new MySubappUserAuthenticationTokenConverter(cairoAuthAppUserService, appUserAuthorizationService);
	}

	@Bean
	TenantAppUserAuthenticationTokenConverter tenantAppUserAuthenticationTokenConverter(CairoAuthTenantAppUserService cairoAuthTenantAppUserService, TenantAppUserAuthorizationService tenantAppUserAuthorizationService) {
		return new MyTenantAppUserAuthenticationTokenConverter(cairoAuthTenantAppUserService, tenantAppUserAuthorizationService);
	}

	@Bean
	TenantSubappUserAuthenticationTokenConverter tenantSubappUserAuthenticationTokenConverter(CairoAuthTenantSubappUserService cairoAuthTenantSubappUserService, TenantAppUserAuthorizationService tenantAppUserAuthorizationService) {
		return new MyTenantSubappUserAuthenticationTokenConverter(cairoAuthTenantSubappUserService, tenantAppUserAuthorizationService);
	}
//
//	@Bean
//	public CairoBearerTokenAuthenticationEntryPoint cairoBearerTokenAuthenticationEntryPoint(HttpMessageConverters messageConverters) {
//		return new CairoBearerTokenAuthenticationEntryPoint(messageConverters.getConverters()).realmName("cairo");
//	}
//
//	@Bean
//	public CairoBearerTokenAccessDeniedHandler cairoBearerTokenAccessDeniedHandler(HttpMessageConverters httpMessageConverters) {
//		return new CairoBearerTokenAccessDeniedHandler(httpMessageConverters.getConverters()).realmName("cairo");
//	}
//
//	@Bean
//	@Primary
//	CairoOAuthJwtAuthenticationConverter cairoOAuthJwtAuthenticationConverter(CairoAuthAccountService cairoAuthAccountService, CairoAuthTenantAppUserService cairoAuthTenantAppUserService,
//																			  CairoAuthTenantAppUserService cairoAuthTenantAppUserService, CairoAuthAppUserService cairoAuthAppUserService,
//																			  CairoAuthAppUserService cairoAuthAppUserService) {
//		return new CairoOAuthJwtAuthenticationConverter(cairoAuthAccountService, cairoAuthTenantAppUserService, cairoAuthTenantAppUserService, cairoAuthAppUserService, cairoAuthAppUserService);
//	}


}
