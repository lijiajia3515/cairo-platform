package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_endpoint;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 创建企业终端参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateTenantEndpointArgs implements Serializable {

	/**
	 * 企业ID
	 */
	@NotNull
	private String tenantId;

	/**
	 * 终端ID
	 */
	@NotNull
	private String endpointId;

	/**
	 * 启用状态
	 */
	@Builder.Default
	private Boolean enabled = true;
}
