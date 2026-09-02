package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取子应用授权参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTenantSubappUserAuthorizationArgs implements Serializable {

	/**
	 * 企业ID
	 */
	@NotNull
	private String tenantId;

	/**
	 * 应用ID
	 */
	@NotNull
	private String appId;

	/**
	 * 终端ID
	 */
	@NotNull
	private String endpointId;

	/**
	 * 访问令牌
	 */
	@NotNull
	private String accessToken;

	/**
	 * 子应用ID
	 */
	@NotNull
	private String subappId;

	/**
	 * 子应用版本
	 */
	@NotNull
	private String subappVersion;
}
