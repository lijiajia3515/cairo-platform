package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCustomTenantAppUserAuthorizationArgs implements Serializable {
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
	 * 子应用ID
	 */
	@NotNull
	private String subappId;

	/**
	 * 子应用版本
	 */
	@NotNull
	private String subappVersion;

	/**
	 * 访问令牌
	 */
	@NotNull
	private String accessToken;
}
