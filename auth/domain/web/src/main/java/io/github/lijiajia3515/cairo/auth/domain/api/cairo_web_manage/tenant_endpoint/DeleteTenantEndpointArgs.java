package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除企业终端
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteTenantEndpointArgs implements Serializable {

	/**
	 * 企业ID
	 */
	@NotBlank
	private String tenantId;

	/**
	 * 应用ID
	 */
	@NotBlank
	private String appId;

	/**
	 * 终端ID
	 */
	@NotBlank
	private String endpointId;
}
