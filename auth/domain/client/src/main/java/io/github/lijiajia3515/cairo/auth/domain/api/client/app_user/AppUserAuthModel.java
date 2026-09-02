package io.github.lijiajia3515.cairo.auth.domain.api.client.app_user;

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
public class AppUserAuthModel {
	private String status;
	/**
	 * 用户信息
	 */
	private AppUserPrincipalModel principal;

	/**
	 * 权限信息
	 */
	private Collection<String> authorities;
}
