package io.github.lijiajia3515.cairo.auth.framework.cairo_auth;

import lombok.Data;

/**
 * 认证客户端配置
 */
@Data
public class AuthClientProperties {

	/**
	 * 客户端注册ID
	 */
	private String clientRegistrationId;

	/**
	 * 客户端ID
	 */
	private String clientId;
	/**
	 * 客户端密钥
	 */
	private String clientSecret;
	/**
	 * 客户端名称
	 */
	private String clientName;
}
