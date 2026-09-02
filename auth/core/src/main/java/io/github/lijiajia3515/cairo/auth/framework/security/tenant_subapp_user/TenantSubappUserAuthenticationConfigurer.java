package io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user;

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

public class TenantSubappUserAuthenticationConfigurer<H extends HttpSecurityBuilder<H>>
	extends AbstractHttpConfigurer<TenantSubappUserAuthenticationConfigurer<H>, H> {

	private final ApplicationContext context;

	private AuthenticationManager authenticationManager;

	private List<HttpMessageConverter<?>> httpMessageConverters;
	private TenantSubappUserAuthenticationTokenConverter tenantSubappUserAuthenticationTokenConverter;
	private AuthenticationEntryPoint authenticationEntryPoint;


	public TenantSubappUserAuthenticationConfigurer(ApplicationContext context) {
		this.context = context;
	}

	public TenantSubappUserAuthenticationConfigurer authenticationManager(AuthenticationManager authenticationManager) {
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

	TenantSubappUserAuthenticationTokenConverter getAppUserTokenAuthenticationConverter() {
		if (this.tenantSubappUserAuthenticationTokenConverter == null) {
			return this.context.getBean(TenantSubappUserAuthenticationTokenConverter.class);
		}
		return this.tenantSubappUserAuthenticationTokenConverter;
	}

	AuthenticationProvider getAuthenticationProvider() {
		if (this.authenticationManager != null) {
			return null;
		}
		TenantSubappUserAuthenticationTokenConverter converter = getAppUserTokenAuthenticationConverter();

		TenantSubappUserTokenAuthenticationProvider provider = new TenantSubappUserTokenAuthenticationProvider();
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
		TenantSubappUserAuthenticationFilter filter = new TenantSubappUserAuthenticationFilter(authenticationManager);
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

	public void setTenantSubappUserAuthenticationTokenConverter(TenantSubappUserAuthenticationTokenConverter tenantSubappUserAuthenticationTokenConverter) {
		this.tenantSubappUserAuthenticationTokenConverter = tenantSubappUserAuthenticationTokenConverter;
	}
}
