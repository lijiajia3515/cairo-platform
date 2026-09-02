package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user;

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
public class TenantAppUserAuthModel {
	private String status;
	/**
	 * 用户信息
	 */
	private TenantAppUserPrincipalModel principal;

	/**
	 * 权限信息
	 */
	private Collection<String> authorities;
}
