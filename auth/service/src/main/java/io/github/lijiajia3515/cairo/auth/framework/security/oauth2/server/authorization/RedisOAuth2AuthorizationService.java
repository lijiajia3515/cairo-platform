
package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization;

import lombok.Setter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

/**
 * authorization redis 实现
 */
public final class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

	private static final String AUTHORIZATION_REDIS_KEY = "oauth2:authorization:%s";

	@Setter
	private Duration expire = Duration.ofDays(30);

	private final RedisTemplate<String, Object> redisTemplate;

	public RedisOAuth2AuthorizationService(RedisConnectionFactory factory) {
		this.redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setHashKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(RedisSerializer.java());
		redisTemplate.setHashValueSerializer(RedisSerializer.java());
		redisTemplate.afterPropertiesSet();
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		redisTemplate.opsForValue().set(authorizationRedisKey(authorization.getId()), authorization, expire);
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		redisTemplate.delete(authorizationRedisKey(authorization.getId()));
	}

	@Nullable
	@Override
	public OAuth2Authorization findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		return (OAuth2Authorization) redisTemplate.opsForValue().get(authorizationRedisKey(id));
	}

	@Nullable
	@Override
	public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
		Assert.hasText(token, "token cannot be empty");
		return Optional.ofNullable(redisTemplate.keys(authorizationRedisKey("*")))
			.orElse(Collections.emptySet())
			.stream().map(x -> (OAuth2Authorization) redisTemplate.opsForValue().get(x))
			.filter(authorization -> hasToken(authorization, token, tokenType))
			.findFirst()
			.orElse(null);
	}

	private static boolean hasToken(OAuth2Authorization authorization, String token, @Nullable OAuth2TokenType tokenType) {
		if (tokenType == null) {
			return matchesState(authorization, token) ||
				matchesAuthorizationCode(authorization, token) ||
				matchesAccessToken(authorization, token) ||
				matchesRefreshToken(authorization, token);
		} else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
			return matchesState(authorization, token);
		} else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
			return matchesAuthorizationCode(authorization, token);
		} else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
			return matchesAccessToken(authorization, token);
		} else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
			return matchesRefreshToken(authorization, token);
		}
		return false;
	}

	private static boolean matchesState(OAuth2Authorization authorization, String token) {
		return token.equals(authorization.getAttribute(OAuth2ParameterNames.STATE));
	}

	private static boolean matchesAuthorizationCode(OAuth2Authorization authorization, String token) {
		OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
			authorization.getToken(OAuth2AuthorizationCode.class);
		return authorizationCode != null && authorizationCode.getToken().getTokenValue().equals(token);
	}

	private static boolean matchesAccessToken(OAuth2Authorization authorization, String token) {
		OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
			authorization.getToken(OAuth2AccessToken.class);
		return accessToken != null && accessToken.getToken().getTokenValue().equals(token);
	}

	private static boolean matchesRefreshToken(OAuth2Authorization authorization, String token) {
		OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
			authorization.getToken(OAuth2RefreshToken.class);
		return refreshToken != null && refreshToken.getToken().getTokenValue().equals(token);
	}

	private static String authorizationRedisKey(String authorizationId) {
		return String.format(AUTHORIZATION_REDIS_KEY, authorizationId);
	}


}
