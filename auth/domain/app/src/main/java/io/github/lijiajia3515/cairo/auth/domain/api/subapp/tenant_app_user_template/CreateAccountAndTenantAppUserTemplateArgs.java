package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 创建账号和企业应用级用户模板参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateAccountAndTenantAppUserTemplateArgs implements Serializable {

	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 邮箱
	 */
	@Email
	private String email;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 密码
	 */
	@Size(min = 6, max = 40)
	private String password;

	/**
	 * 昵称
	 */
	@NotNull
	private String nickname;

	/**
	 * 角色
	 */
	private List<String> tenantAppRoleTemplateIds;

	/**
	 * 部门
	 */
	private List<String> tenantAppDepartmentTemplateIds;


	/**
	 * 职位
	 */
	private String position;

	/**
	 * 主部门id
	 */
	private String tenantMainDepartmentTemplateId;

}
