package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 根据企业用户模板ID获取用户信息参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTenantAppUserTemplateInfoArgs implements Serializable {

	/**
	 * 企业用户模板ID
	 */
	@NotNull
	private String tenantAppUserTemplateId;
}
