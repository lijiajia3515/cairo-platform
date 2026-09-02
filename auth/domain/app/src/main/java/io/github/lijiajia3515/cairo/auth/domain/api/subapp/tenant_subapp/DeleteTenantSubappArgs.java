package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_subapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除企业子应用
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteTenantSubappArgs implements Serializable {

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
	 * 终端ID
	 */
	@NotNull
	private String subappId;
}
