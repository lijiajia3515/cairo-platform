package io.github.lijiajia3515.cairo.auth.domain.api.open.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 根据企业名称获取企业
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetTenantByTenantNameArgs implements Serializable {
	/**
	 * 企业名称
	 */
	@NotNull
	@NotBlank
	private String tenantName;

}
