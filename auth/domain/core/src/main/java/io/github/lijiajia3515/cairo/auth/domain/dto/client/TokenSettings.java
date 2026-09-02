package io.github.lijiajia3515.cairo.auth.domain.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Duration;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenSettings {
	/**
	 * id token signature algorithm
	 */
	private String idTokenSignatureAlgorithm;

	/**
	 * id token 格式
	 */
	private String idTokenFormat;

	/**
	 * id token 有效期
	 */
	private Duration idTokenTimeToLive;

	/**
	 * access token 格式
	 */
	private String accessTokenFormat;

	/**
	 * access token 有效期
	 */
	private Duration accessTokenTimeToLive;

	/**
	 * refresh token 有效期
	 */
	private Duration refreshTokenTimeToLive;

	/**
	 * 是否允许重用refreshToken刷新accessToken
	 */
	private Boolean reuseRefreshTokens;

	/**
	 * account access token 格式
	 */
	private String accountAccessTokenFormat;


	/**
	 * account access token 有效期
	 */
	private Duration accountAccessTokenTimeToLive;

	/**
	 * account refresh token 有效期
	 */
	private Duration accountRefreshTokenTimeToLive;

	/**
	 * 是否允许重用accountRefreshToken刷新accessToken
	 */
	private Boolean reuseAccountRefreshTokens;

	/**
	 *  app endpoint user access token 格式
	 */
	private String appUserAccessTokenFormat;

	/**
	 * app endpoint user access token 有效期
	 */
	private Duration appUserAccessTokenTimeToLive;

	/**
	 *  app endpoint user refresh token 有效期
	 */
	private Duration appUserRefreshTokenTimeToLive;

	/**
	 * 是否允许重用 appUserRefreshToken刷新accessToken
	 */
	private Boolean reuseAppUserRefreshTokens;

	/**
	 * tenant app  endpoint user access token 格式
	 */
	private String tenantAppUserAccessTokenFormat;

	/**
	 * tenant app  endpoint user access token 有效期
	 */
	private Duration tenantAppUserAccessTokenTimeToLive;

	/**
	 * tenant app endpoint user refresh token 有效期
	 */
	private Duration tenantAppUserRefreshTokenTimeToLive;

	/**
	 * 是否允许重用TenantAppUserRefreshToken刷新accessToken
	 */
	private Boolean reuseTenantAppUserRefreshTokens;



}
