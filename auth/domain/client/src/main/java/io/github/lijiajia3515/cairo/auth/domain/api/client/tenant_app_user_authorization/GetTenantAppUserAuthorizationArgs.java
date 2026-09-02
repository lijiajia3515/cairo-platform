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
public class GetTenantAppUserAuthorizationArgs implements Serializable {

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
	 * 访问令牌
	 */
	@NotNull
	private String accessToken;
}
