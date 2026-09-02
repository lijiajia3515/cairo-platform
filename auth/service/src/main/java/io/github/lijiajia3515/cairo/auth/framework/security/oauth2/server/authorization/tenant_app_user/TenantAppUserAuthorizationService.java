/*
 * Copyright 2020-2022 the original author or authors.
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
package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

/**
 * Implementations of this interface are responsible for the management of
 * {@link org.springframework.security.oauth2.server.authorization.OAuth2Authorization OAuth 2.0 Authorization(s)}.
 *
 * @author Joe Grandja
 * @see org.springframework.security.oauth2.server.authorization.OAuth2Authorization
 * @see org.springframework.security.oauth2.server.authorization.OAuth2TokenType
 * @since 0.0.1
 */
public interface TenantAppUserAuthorizationService {


	void save(OAuth2Authorization authorization);


	void remove(OAuth2Authorization authorization);


	@Nullable
	OAuth2Authorization findById(String tenantId, String appId, String endpointId, String id);


	@Nullable
	OAuth2Authorization findByToken(String tenantId, String appId, String endpointId, String token, @Nullable OAuth2TokenType tokenType);

}
