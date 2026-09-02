package io.github.lijiajia3515.cairo.auth.domain.api.client.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公众号认证信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxmpProviderInfo {
	/**
	 * 标识
	 */
	private String providerId;

	/**
	 * 名称
	 */
	private String providerName;


	/**
	 * 设置微信公众号的appid.
	 */
	private String wxmpAppId;

	/**
	 * 设置微信公众号的app secret.
	 */
	private String wxmpSecret;

	/**
	 * 设置微信公众号的token.
	 */
	private String wxmpToken;

	/**
	 * 设置微信公众号的EncodingAESKey.
	 */
	private String wxmpAesKey;

	/**
	 * 是否启用（启用后，可以发送，未启用不会发送）
	 */
	private boolean enabled;
}
