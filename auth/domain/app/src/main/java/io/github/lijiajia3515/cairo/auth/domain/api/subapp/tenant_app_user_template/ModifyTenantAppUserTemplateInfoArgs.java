package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 修改企业应用级用户模板信息
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyTenantAppUserTemplateInfoArgs implements Serializable {

	/**
	 * 用户id
	 */
	@NotNull
	private String tenantAppUserTemplateId;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 联系方式
	 */
	private String phoneNumber;

	/**
	 * 职位
	 */
	private String position;

	/**
	 * 主部门id
	 */
	private String tenantMainDepartmentTemplateId;



	/**
	 * 角色
	 */
	private List<String> tenantAppRoleTemplateIds;

	/**
	 * 部门
	 */
	private List<String> tenantAppDepartmentTemplateIds;



}
