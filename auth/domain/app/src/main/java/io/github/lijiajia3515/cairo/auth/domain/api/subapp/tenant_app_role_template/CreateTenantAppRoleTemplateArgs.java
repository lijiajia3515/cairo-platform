package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 创建企业角色模板参数
 */
@Data
@Accessors(chain = true)
public class CreateTenantAppRoleTemplateArgs implements Serializable {
	/**
	 * 企业角色模板ID
	 */
	private String tenantAppRoleTemplateId;

	/**
	 * 名称
	 */
	@NotNull
	private String tenantAppRoleTemplateName;

	/**
	 * 备注
	 */
	private String remark;
}
