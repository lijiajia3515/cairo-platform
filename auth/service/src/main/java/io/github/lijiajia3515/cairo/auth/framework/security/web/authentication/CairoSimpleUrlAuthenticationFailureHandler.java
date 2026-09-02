/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lijiajia3515.cairo.auth.framework.security.web.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class CairoSimpleUrlAuthenticationFailureHandler implements AuthenticationFailureHandler {


	private String defaultFailureUrl;

	private boolean forwardToDestination = false;

	private boolean allowSessionCreation = true;

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public CairoSimpleUrlAuthenticationFailureHandler() {
	}

	public CairoSimpleUrlAuthenticationFailureHandler(String defaultFailureUrl) {
		setDefaultFailureUrl(defaultFailureUrl);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
										AuthenticationException exception) throws IOException, ServletException {
		log.info("[AuthenticationFailure] Request: {}, Error: {}]", request.getRequestURI(), exception.getMessage());
		log.info("Exception: ", exception);
		if (this.defaultFailureUrl == null) {
			if (log.isTraceEnabled()) {
				log.trace("Sending 401 Unauthorized error since no failure URL is set");
			} else {
				log.debug("Sending 401 Unauthorized error");
			}
			response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
			return;
		}
		saveException(request, exception);
		if (this.forwardToDestination) {
			log.debug("Forwarding to " + this.defaultFailureUrl);
			request.getRequestDispatcher(this.defaultFailureUrl).forward(request, response);
		} else {
			this.redirectStrategy.sendRedirect(request, response, this.defaultFailureUrl);
		}
	}

	protected final void saveException(HttpServletRequest request, AuthenticationException exception) {
		if (this.forwardToDestination) {
			request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
			return;
		}
		HttpSession session = request.getSession(false);
		if (session != null || this.allowSessionCreation) {
			request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
		}
	}

	public void setDefaultFailureUrl(String defaultFailureUrl) {
		Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl),
			() -> "'" + defaultFailureUrl + "' is not a valid redirect URL");
		this.defaultFailureUrl = defaultFailureUrl;
	}

	protected boolean isUseForward() {
		return this.forwardToDestination;
	}

	public void setUseForward(boolean forwardToDestination) {
		this.forwardToDestination = forwardToDestination;
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	protected RedirectStrategy getRedirectStrategy() {
		return this.redirectStrategy;
	}

	protected boolean isAllowSessionCreation() {
		return this.allowSessionCreation;
	}

	public void setAllowSessionCreation(boolean allowSessionCreation) {
		this.allowSessionCreation = allowSessionCreation;
	}

}
