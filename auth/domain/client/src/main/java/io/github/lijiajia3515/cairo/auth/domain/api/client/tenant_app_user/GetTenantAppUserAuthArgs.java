package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user;

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
public class GetTenantAppUserAuthArgs implements Serializable {

	/**
	 * 企业id
	 */
	@NotNull
	private String tenantId;

	/**
	 * appId
	 */
	@NotNull
	private String appId;

	/**
	 * endpointId
	 */
	@NotNull
	private String endpointId;

	/**
	 * clientId
	 */
	@NotNull
	private String clientId;
	/**
	 * 用户id
	 */
	@NotNull
	private String userId;
}
