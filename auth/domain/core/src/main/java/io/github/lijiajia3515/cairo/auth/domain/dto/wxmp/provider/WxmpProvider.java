package io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.provider;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 微信公众号连接配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxmpProvider {

	/**
	 * 标识
	 */
	private String wxmpProviderId;


	/**
	 * 名称
	 */
	private String wxmpProviderName;


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
