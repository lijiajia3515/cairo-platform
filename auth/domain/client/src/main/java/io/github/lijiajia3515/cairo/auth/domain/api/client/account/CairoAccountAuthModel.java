package io.github.lijiajia3515.cairo.auth.domain.api.client.account;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CairoAccountAuthModel {
	/**
	 * 认证状态
	 */
	private String status;

	/**
	 * 错误信息
	 */
	private String errorMessage;

	/**
	 * 账号信息
	 */
	private CairoOAuthAccountPrincipal principal;

	/**
	 * 权限信息
	 */
	private Collection<String> authorities;
}
