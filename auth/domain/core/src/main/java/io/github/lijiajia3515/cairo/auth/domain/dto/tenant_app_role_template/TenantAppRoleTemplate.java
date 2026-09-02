package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业角色模板信息
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppRoleTemplate implements Serializable {
	/**
	 * 角色标识
	 */
	private String tenantAppRoleTemplateId;

	/**
	 * 名称
	 */
	private String tenantAppRoleTemplateName;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 用户数量
	 */
	private Integer userNum;

	/**
	 * 启用状态
	 */
	private Boolean enabled;
}
