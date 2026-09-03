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

import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings.CairoSettingNames;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * endpoint user refresh token generator
 */
public final class AppUserRefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {
    private final StringKeyGenerator refreshTokenGenerator = new TokenKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 32, "app_user_rt_");

    @Nullable
    @Override
    public OAuth2RefreshToken generate(OAuth2TokenContext context) {
        if (!CairoOAuthTokenTypeConstants.APP_USER_REFRESH_TOKEN.equals(context.getTokenType())) {
            return null;
        }
        Authentication authentication = context.getPrincipal();
        if (!(authentication.getPrincipal() instanceof CairoAuthAppUser)) {
            return null;
        }

		CairoAuthAppUser user = (CairoAuthAppUser) authentication.getPrincipal();

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus((Duration) context.getRegisteredClient().getTokenSettings().getSettings().getOrDefault(CairoSettingNames.Token.APP_USER_REFRESH_TOKEN_TIME_TO_LIVE, Duration.ofDays(7)));
        return new OAuthAppUserRefreshToken(this.refreshTokenGenerator.generateKey(), issuedAt, expiresAt);
    }

}
