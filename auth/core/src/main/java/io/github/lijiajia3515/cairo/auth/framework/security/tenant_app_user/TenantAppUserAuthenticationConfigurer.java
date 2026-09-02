package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.account.AccountAuthenticationFilter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

import java.util.List;

public class TenantAppUserAuthenticationConfigurer<H extends HttpSecurityBuilder<H>>
	extends AbstractHttpConfigurer<TenantAppUserAuthenticationConfigurer<H>, H> {

	private final ApplicationContext context;

	private AuthenticationManager authenticationManager;

	private List<HttpMessageConverter<?>> httpMessageConverters;
	private TenantAppUserAuthenticationTokenConverter tenantAppUserAuthenticationTokenConverter;
	private AuthenticationEntryPoint authenticationEntryPoint;


	public TenantAppUserAuthenticationConfigurer(ApplicationContext context) {
		this.context = context;
	}

	public TenantAppUserAuthenticationConfigurer authenticationManager(AuthenticationManager authenticationManager) {
		Assert.notNull(authenticationManager, "authenticationManager cannot be null");
		this.authenticationManager = authenticationManager;
		return this;
	}

	List<HttpMessageConverter<?>> getHttpMessageConverters() {
		if (this.httpMessageConverters == null) {
			return this.context.getBean(HttpMessageConverters.class).getConverters();
		}
		return this.httpMessageConverters;
	}

	AuthenticationEntryPoint getAuthenticationEntryPoint() {
		if (this.authenticationEntryPoint == null) {
			return this.context.getBean(AuthenticationEntryPoint.class);
		}
		return this.authenticationEntryPoint;
	}

	TenantAppUserAuthenticationTokenConverter getAppUserTokenAuthenticationConverter() {
		if (this.tenantAppUserAuthenticationTokenConverter == null) {
			return this.context.getBean(TenantAppUserAuthenticationTokenConverter.class);
		}
		return this.tenantAppUserAuthenticationTokenConverter;
	}

	AuthenticationProvider getAuthenticationProvider() {
		if (this.authenticationManager != null) {
			return null;
		}
		TenantAppUserAuthenticationTokenConverter converter = getAppUserTokenAuthenticationConverter();

		TenantAppUserTokenAuthenticationProvider provider = new TenantAppUserTokenAuthenticationProvider();
		provider.setAuthenticationConverter(converter);
		return postProcess(provider);
	}

	AuthenticationManager getAuthenticationManager(H http) {
		if (this.authenticationManager != null) {
			return this.authenticationManager;
		}
		return http.getSharedObject(AuthenticationManager.class);
	}

	@Override
	public void init(H http) throws Exception {
		super.init(http);
		AuthenticationProvider authenticationProvider = getAuthenticationProvider();
		if (authenticationProvider != null) {
			http.authenticationProvider(authenticationProvider);
		}
	}

	@Override
	public void configure(H http) {
		AuthenticationManager authenticationManager = getAuthenticationManager(http);
		TenantAppUserAuthenticationFilter filter = new TenantAppUserAuthenticationFilter(authenticationManager);
		filter.setAuthenticationEntryPoint(getAuthenticationEntryPoint());
		filter = postProcess(filter);
		http.addFilterAfter(filter, AccountAuthenticationFilter.class);
	}

	public void setHttpMessageConverters(List<HttpMessageConverter<?>> httpMessageConverters) {
		this.httpMessageConverters = httpMessageConverters;
	}

	public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	public void setTenantAppUserAuthenticationTokenConverter(TenantAppUserAuthenticationTokenConverter tenantAppUserAuthenticationTokenConverter) {
		this.tenantAppUserAuthenticationTokenConverter = tenantAppUserAuthenticationTokenConverter;
	}
}
