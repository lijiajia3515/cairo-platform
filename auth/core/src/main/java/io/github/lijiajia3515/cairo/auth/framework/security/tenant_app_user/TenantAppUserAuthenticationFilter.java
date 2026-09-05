

package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates requests that contain an OAuth 2.0
 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer
 * Token</a>.
 *
 * This filter should be wired with an {@link org.springframework.security.authentication.AuthenticationManager} that can authenticate
 * a {@link org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken}.
 *
 * @author Josh Cummings
 * @author Vedran Pavic
 * @author Joe Grandja
 * @author Jeongjin Kim
 * @since 5.1
 * @see <a href="https://tools.ietf.org/html/rfc6750" target="_blank">The OAuth 2.0
 * Authorization Framework: Bearer Token Usage</a>
 * @see org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
 */
public class TenantAppUserAuthenticationFilter extends OncePerRequestFilter {

	private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

	private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

	private AuthenticationEntryPoint authenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();

	private AuthenticationFailureHandler authenticationFailureHandler = new AuthenticationEntryPointFailureHandler(
			(request, response, exception) -> this.authenticationEntryPoint.commence(request, response, exception));

	private TenantAppUserAuthenticationTokenResolver tenantAppUserAuthenticationTokenResolver = new TenantAppUserAuthenticationTokenResolver();

	private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

	private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

	/**
	 * Construct a {@code BearerTokenAuthenticationFilter} using the provided parameter(s)
	 * @param authenticationManagerResolver
	 */
	public TenantAppUserAuthenticationFilter(
			AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) {
		Assert.notNull(authenticationManagerResolver, "authenticationManagerResolver cannot be null");
		this.authenticationManagerResolver = authenticationManagerResolver;
	}

	/**
	 * Construct a {@code BearerTokenAuthenticationFilter} using the provided parameter(s)
	 * @param authenticationManager
	 */
	public TenantAppUserAuthenticationFilter(AuthenticationManager authenticationManager) {
		Assert.notNull(authenticationManager, "authenticationManager cannot be null");
		this.authenticationManagerResolver = (request) -> authenticationManager;
	}

	/**
	 * Extract any
	 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer
	 * Token</a> from the request and attempt an authentication.
	 * @param request
	 * @param response
	 * @param filterChain
	 * @throws jakarta.servlet.ServletException
	 * @throws java.io.IOException
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		TenantAppUserAuthenticationTokenRequest authenticationRequest;
		try {
			authenticationRequest = this.tenantAppUserAuthenticationTokenResolver.resolve(request);
		}
		catch (AuthenticationException invalid) {
			this.logger.trace("Sending to authentication entry point since failed to resolve app user authenticationRequest", invalid);
			this.authenticationEntryPoint.commence(request, response, invalid);
			return;
		}
		if (authenticationRequest == null) {
			this.logger.trace("Did not process request since did not find bearer authenticationRequest");
			filterChain.doFilter(request, response);
			return;
		}

		authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

		try {
			AuthenticationManager authenticationManager = this.authenticationManagerResolver.resolve(request);
			Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);
			SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
			context.setAuthentication(authenticationResult);
			this.securityContextHolderStrategy.setContext(context);
			this.securityContextRepository.saveContext(context, request, response);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authenticationResult));
			}
			filterChain.doFilter(request, response);
		}
		catch (AuthenticationException failed) {
			this.securityContextHolderStrategy.clearContext();
			this.logger.trace("Failed to process authentication request", failed);
			this.authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
		}
	}

	/**
	 * Sets the {@link org.springframework.security.core.context.SecurityContextHolderStrategy} to use. The default action is to use
	 * the {@link org.springframework.security.core.context.SecurityContextHolderStrategy} stored in {@link org.springframework.security.core.context.SecurityContextHolder}.
	 *
	 * @since 5.8
	 */
	public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy securityContextHolderStrategy) {
		Assert.notNull(securityContextHolderStrategy, "securityContextHolderStrategy cannot be null");
		this.securityContextHolderStrategy = securityContextHolderStrategy;
	}

	/**
	 * Sets the {@link org.springframework.security.web.context.SecurityContextRepository} to save the {@link org.springframework.security.core.context.SecurityContext} on
	 * authentication success. The default action is not to save the
	 * {@link org.springframework.security.core.context.SecurityContext}.
	 * @param securityContextRepository the {@link org.springframework.security.web.context.SecurityContextRepository} to use.
	 * Cannot be null.
	 */
	public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
		Assert.notNull(securityContextRepository, "securityContextRepository cannot be null");
		this.securityContextRepository = securityContextRepository;
	}

	/**
	 * Set the {@link org.springframework.security.oauth2.server.resource.web.BearerTokenResolver} to use. Defaults to
	 * {@link org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver}.
	 * @param tenantAppUserAuthenticationTokenResolver the {@code BearerTokenResolver} to use
	 */
	public void setAccountTokenResolver(TenantAppUserAuthenticationTokenResolver tenantAppUserAuthenticationTokenResolver) {
		Assert.notNull(tenantAppUserAuthenticationTokenResolver, "bearerTokenResolver cannot be null");
		this.tenantAppUserAuthenticationTokenResolver = tenantAppUserAuthenticationTokenResolver;
	}

	/**
	 * Set the {@link org.springframework.security.web.AuthenticationEntryPoint} to use. Defaults to
	 * {@link org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint}.
	 * @param authenticationEntryPoint the {@code AuthenticationEntryPoint} to use
	 */
	public void setAuthenticationEntryPoint(final AuthenticationEntryPoint authenticationEntryPoint) {
		Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint cannot be null");
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	/**
	 * Set the {@link org.springframework.security.web.authentication.AuthenticationFailureHandler} to use. Default implementation invokes
	 * {@link org.springframework.security.web.AuthenticationEntryPoint}.
	 * @param authenticationFailureHandler the {@code AuthenticationFailureHandler} to use
	 * @since 5.2
	 */
	public void setAuthenticationFailureHandler(final AuthenticationFailureHandler authenticationFailureHandler) {
		Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
		this.authenticationFailureHandler = authenticationFailureHandler;
	}

	/**
	 * Set the {@link org.springframework.security.authentication.AuthenticationDetailsSource} to use. Defaults to
	 * {@link org.springframework.security.web.authentication.WebAuthenticationDetailsSource}.
	 * @param authenticationDetailsSource the {@code AuthenticationConverter} to use
	 * @since 5.5
	 */
	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
		Assert.notNull(authenticationDetailsSource, "authenticationDetailsSource cannot be null");
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

}
