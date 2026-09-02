package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_endpoint;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改企业终端状态 参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyTenantEndpointStatusArgs implements Serializable {

	/**
	 * 企业ID
	 */
	@NotBlank
	private String tenantId;
	/**
	 * 终端ID
	 */
	@NotBlank
	private String endpointId;


	/**
	 * 状态
	 */
	private Boolean enabled;
}
