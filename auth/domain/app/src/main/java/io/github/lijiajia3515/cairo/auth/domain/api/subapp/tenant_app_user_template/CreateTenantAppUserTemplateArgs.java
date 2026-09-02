package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 创建企业用户模板参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateTenantAppUserTemplateArgs implements Serializable {

	/**
	 * 账号id
	 */
	@NotNull
	private String accountId;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 联系方式
	 */
	private String phoneNumber;

	/**
	 * 主部门id
	 */
	private String tenantMainDepartmentTemplateId;



	/**
	 * 职位
	 */
	private String position;

	/**
	 * 角色
	 */
	private List<String> tenantAppRoleTemplateIds;

	/**
	 * 部门
	 */
	private List<String> tenantAppDepartmentTemplateIds;


}
