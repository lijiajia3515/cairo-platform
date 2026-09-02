/*
 * Copyright 2002-2023 the original author or authors.
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

package io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class TenantSubappUserAuthenticationTokenResolver {

	public static final String TENANT_ID = "tenantId";
	public static final String APP_ID = "appId";
	public static final String ENDPOINT_ID = "endpointId";
	public static final String SUBAPP_ID = "subappId";
	public static final String SUBAPP_VERSION = "subappVersion";
	public static final String TOKEN = "token";

	private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("^tenant_subapp_user (?<tenantId>[a-zA-Z0-9-._~+]+=*)/(?<appId>[a-zA-Z0-9-._~+]+=*)/(?<endpointId>[a-zA-Z0-9-._~+]+=*)/(?<subappId>[a-zA-Z0-9-._~+]+=*)/(?<subappVersion>[a-zA-Z0-9-._~+]+)/(?<token>[a-zA-Z0-9-._~+]+=*)$", Pattern.CASE_INSENSITIVE);

	private String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;

	public TenantSubappUserAuthenticationTokenRequest resolve(final HttpServletRequest request) {
		return resolveFromAuthorizationHeader(request);
	}


	/**
	 * Set this value to configure what header is checked when resolving a Bearer Token.
	 * This value is defaulted to {@link org.springframework.http.HttpHeaders#AUTHORIZATION}.
	 * <p>
	 * This allows other headers to be used as the Bearer Token source such as
	 * {@link org.springframework.http.HttpHeaders#PROXY_AUTHORIZATION}
	 *
	 * @param bearerTokenHeaderName the header to check when retrieving the Bearer Token.
	 * @since 5.4
	 */
	public void setBearerTokenHeaderName(String bearerTokenHeaderName) {
		this.bearerTokenHeaderName = bearerTokenHeaderName;
	}

	private TenantSubappUserAuthenticationTokenRequest resolveFromAuthorizationHeader(HttpServletRequest request) {
		String authorization = request.getHeader(this.bearerTokenHeaderName);
		if (!StringUtils.startsWithIgnoreCase(authorization, "tenant_subapp_user")) {
			return null;
		}
		Matcher matcher = AUTHORIZATION_PATTERN.matcher(authorization);
		if (!matcher.matches()) {
			throw new TokenInvalidException("tenant_subapp_user 认证参数错误");
		}

		return new TenantSubappUserAuthenticationTokenRequest(
			matcher.group(TENANT_ID),
			matcher.group(APP_ID),
			matcher.group(ENDPOINT_ID),
			matcher.group(SUBAPP_ID),
			matcher.group(SUBAPP_VERSION),
			matcher.group(TOKEN)
		);
	}
}
