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
public class WxmpOpenIdInfo {

	/**
	 * 供应商ID
	 */
	private String providerId;

	/**
	 * 公众号ID
	 */
	private String appId;

	/**
	 * 微信公众号OpenId
	 */
	private String openId;
}
