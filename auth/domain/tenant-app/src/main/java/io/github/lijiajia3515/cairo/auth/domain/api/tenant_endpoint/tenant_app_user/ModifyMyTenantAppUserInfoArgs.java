package io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 修改用户信息
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ModifyMyTenantAppUserInfoArgs implements Serializable {

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 联系方式
	 */
	private String phoneNumber;

}
