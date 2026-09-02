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
package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token;

import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings.CairoSettingNames;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.TENANT_APP_USER;

/**
 * user access token jwt generator
 */
public final class TenantAppUserAccessTokenJwtGenerator implements OAuth2TokenGenerator<Jwt> {
	private final JwtEncoder jwtEncoder;
	private OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer;

	/**
	 * Constructs a {@code JwtGenerator} using the provided parameters.
	 *
	 * @param jwtEncoder the jwt encoder
	 */
	public TenantAppUserAccessTokenJwtGenerator(JwtEncoder jwtEncoder) {
		Assert.notNull(jwtEncoder, "jwtEncoder cannot be null");
		this.jwtEncoder = jwtEncoder;
	}

	@Nullable
	@Override
	public Jwt generate(OAuth2TokenContext context) {
		CairoRegisteredClient registeredClient = (CairoRegisteredClient) context.getRegisteredClient();
		if (context.getTokenType() == null || !CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN.equals(context.getTokenType())) {
			return null;
		}
		OAuth2TokenFormat tokenFormat = (OAuth2TokenFormat) context.getRegisteredClient().getTokenSettings().getSettings().getOrDefault(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_FORMAT, OAuth2TokenFormat.SELF_CONTAINED);
		if (!OAuth2TokenFormat.SELF_CONTAINED.equals(tokenFormat)) {
			return null;
		}

		Authentication authentication = context.getPrincipal();
		CairoAuthTenantAppUser user;
		if (!(authentication.getPrincipal() instanceof CairoAuthTenantAppUser)) {
			return null;
		}
		user = (CairoAuthTenantAppUser) authentication.getPrincipal();

		String issuer = null;
		if (context.getAuthorizationServerContext() != null) {
			issuer = context.getAuthorizationServerContext().getIssuer();
		}

		// oauth basic
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus((Duration) registeredClient.getTokenSettings().getSettings().getOrDefault(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_TIME_TO_LIVE, Duration.ofMinutes(30)));
		JwsAlgorithm jwsAlgorithm = SignatureAlgorithm.RS256;

		JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
		if (StringUtils.hasText(issuer)) {
			claimsBuilder.issuer(issuer);
		}

		claimsBuilder
			.subject(context.getAuthorization().getId())
			.audience(Collections.singletonList(registeredClient.getClientId()))
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.notBefore(issuedAt);
		if (!CollectionUtils.isEmpty(context.getAuthorizedScopes())) {
			claimsBuilder.claim(OAuth2ParameterNames.SCOPE, context.getAuthorizedScopes());
		}

		// user claim
		claimsBuilder
			.claim(CairoOAuthParameterNames.AUTH_TYPE, TENANT_APP_USER.getValue())
			.claim(CairoOAuthParameterNames.APP_ID, registeredClient.getAppId()) // app id
			.claim(CairoOAuthParameterNames.ENDPOINT_ID, user.getEndpointId()) // app endpoint id
			.claim(OAuth2ParameterNames.CLIENT_ID, registeredClient.getClientId()) // client id
			.claim(CairoOAuthParameterNames.TENANT_ID, user.getTenantId()) // tenantId
			.claim(CairoOAuthParameterNames.USER_ID, user.getUserId()) // userId
			.claim(CairoOAuthParameterNames.LOGIN_TYPE, user.getLoginType().getValue()) // login_type
		;
		if (user.getSnsType() != null && !user.getSnsType().isBlank()) {
			claimsBuilder.claim(CairoOAuthParameterNames.SNS_TYPE, user.getSnsType()); // login_type
		}

		JwsHeader.Builder jwsHeaderBuilder = JwsHeader.with(jwsAlgorithm);

		if (this.jwtCustomizer != null) {
			JwtEncodingContext.Builder jwtContextBuilder = JwtEncodingContext.with(jwsHeaderBuilder, claimsBuilder)
				.registeredClient(context.getRegisteredClient())
				.principal(context.getPrincipal())
				.authorizationServerContext(context.getAuthorizationServerContext())
				.authorizedScopes(context.getAuthorizedScopes())
				.tokenType(context.getTokenType())
				.authorizationGrantType(context.getAuthorizationGrantType());
			if (context.getAuthorization() != null) {
				jwtContextBuilder.authorization(context.getAuthorization());
			}
			if (context.getAuthorizationGrant() != null) {
				jwtContextBuilder.authorizationGrant(context.getAuthorizationGrant());
			}

			JwtEncodingContext jwtContext = jwtContextBuilder.build();
			this.jwtCustomizer.customize(jwtContext);
		}

		JwsHeader jwsHeader = jwsHeaderBuilder.build();
		JwtClaimsSet claims = claimsBuilder.build();

		Jwt jwt = this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));

		return jwt;
	}

	/**
	 * Sets the {@link OAuth2TokenCustomizer} that customizes the
	 * {@link JwtEncodingContext#getJwsHeader() JWS headers} and/or
	 * {@link JwtEncodingContext#getClaims() claims} for the generated {@link Jwt}.
	 *
	 * @param jwtCustomizer the {@link OAuth2TokenCustomizer} that customizes the headers and/or claims for the generated {@code Jwt}
	 */
	public void setJwtCustomizer(OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
		Assert.notNull(jwtCustomizer, "jwtCustomizer cannot be null");
		this.jwtCustomizer = jwtCustomizer;
	}

}
