package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改企业角色模板参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyTenantAppRoleTemplateInfoArgs implements Serializable {

	/**
	 * id
	 */
	@NotNull
	@NotBlank
	private String tenantAppRoleTemplateId;



	/**
	 * 名称
	 */
	private String tenantAppRoleTemplateName;

	/**
	 * 备注
	 */
	private String remark;

}
