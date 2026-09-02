package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业角色子应用子应用版本
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppRoleTemplateSubappVersion implements Serializable {

	/**
	 * 子应用ID
	 */
	private String subappId;


	/**
	 * 子应用版本
	 */
	private String subappVersion;

	/**
	 * 子应用备注
	 */
	private String subappRemark;

	/**
	 * 是否启用
	 */
	private Boolean enabled;


}
