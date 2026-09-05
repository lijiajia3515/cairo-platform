package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.PathTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;


/**
 * 企业应用级用户模板
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MetadataTenantAppUserTemplate implements Serializable {
	/**
	 * Id
	 */
	private String tenantAppUserTemplateId;


	/**
	 * 昵称
	 */
	private String nickname;


	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 是否应用管理员
	 */
	private Boolean appAdmin;

	/**
	 * 角色
	 */
	private List<TenantAppRoleTemplate> tenantAppRoleTemplates;

	/**
	 * 部门
	 */
	private List<PathTenantAppDepartmentTemplate> tenantAppDepartmentTemplates;

	/**
	 * 主部门
	 */
	private String tenantMainDepartmentTemplateId;



	/**
	 * 启用状态
	 */
	private Boolean enabled;


	/**
	 * accountId
	 */
	private String accountId;


	/**
	 * 职位
	 */
	private String position;

	/**
	 * accountNickname
	 */
	private String accountNickname;

	/**
	 * 头像
	 */
	private String accountAvatarUrl;

	/**
	 * 账号登录手机号
	 */
	private String accountPhoneNumber;

	/**
	 * 用户名
	 */
	private String accountUsername;

	/**
	 * 邮箱
	 */
	private String accountEmail;

	/**
	 * 元信息
	 */
	private CairoAppUserMetadata metadata;

}
