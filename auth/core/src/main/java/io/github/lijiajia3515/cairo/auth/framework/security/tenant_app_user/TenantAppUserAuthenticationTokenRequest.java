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

package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Collections;

/**
 * An {@link org.springframework.security.core.Authentication} that contains a
 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer
 * Token</a>.
 * <p>
 * Used by {@link org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter} to prepare an authentication attempt
 * and supported by {@link org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider}.
 *
 * @author Josh Cummings
 * @since 5.1
 */
public class TenantAppUserAuthenticationTokenRequest extends AbstractAuthenticationToken {
	@Getter
	private final String tenantId;
	@Getter
	private final String appId;
	@Getter
	private final String endpointId;
	
	private final String token;

	/**
	 * 创建账号token
	 *
	 * @param token token字符串
	 */
	public TenantAppUserAuthenticationTokenRequest(String tenantId, String appId, String endpointId, String token) {
		super(Collections.emptyList());
		Assert.hasText(tenantId, "tenantId cannot be empty");
		Assert.hasText(appId, "appId cannot be empty");
		Assert.hasText(endpointId, "endpointId cannot be empty");
		Assert.hasText(token, "token cannot be empty");
		this.tenantId = tenantId;
		this.appId = appId;
		this.endpointId = endpointId;
		this.token = token;
	}

	/**
	 * 获取token
	 *
	 * @return token字符串
	 */
	public String getToken() {
		return this.token;
	}

	@Override
	public Object getCredentials() {
		return this.getToken();
	}

	@Override
	public Object getPrincipal() {
		return this.getToken();
	}

}
