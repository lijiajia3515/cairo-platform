package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CairoOAuthClientPrincipal implements Serializable {
	/**
	 * 唯一id
	 */
	private String id;

	/**
	 * 登录方式
	 */
	private String loginType;

	/**
	 * 应用id
	 */
	private String appId;

	/**
	 * 客户端id
	 */
	private String clientId;

}
