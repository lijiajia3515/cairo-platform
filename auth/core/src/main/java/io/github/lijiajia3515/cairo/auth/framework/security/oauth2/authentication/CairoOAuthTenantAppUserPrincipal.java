package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication;

import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.jackson.desensitize.Desensitize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

import static io.github.lijiajia3515.cairo.jackson.desensitize.DesensitizeType.EMAIL;
import static io.github.lijiajia3515.cairo.jackson.desensitize.DesensitizeType.MOBILE_PHONE;

/**
 * 企业终端用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CairoOAuthTenantAppUserPrincipal implements Serializable {
	/**
	 * 唯一id
	 */
	private String id;

	/**
	 * 登录方式
	 */
	private LoginType loginType;

	/**
	 * 第三方认证类型
	 */
	private String snsType;

	/**
	 * 租户id
	 */
	private String tenantId;

	/**
	 * 应用id
	 */
	private String appId;

	/**
	 * 端点ID
	 */
	private String endpointId;


	/**
	 * 客户端id
	 */
	private String clientId;

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 联系方式
	 */
	private String phoneNumber;

	/**
	 * 用户启用状态
	 */
	private Boolean userEnabled;

	/**
	 * 是否管理员
	 */
	private Boolean appAdmin;

	/**
	 * 角色
	 */
	private List<CairoRole> roles;

	/**
	 * 部门
	 */
	private List<CairoDepartment> departments;

	/**
	 * 职位
	 */
	private String position;


	/**
	 * 标签
	 */
	private List<CairoTag> tags;

	// account field
	/**
	 * 账号id
	 */
	private String accountId;

	/**
	 * 账号头像
	 */
	private String accountAvatarUrl;

	/**
	 * 账号昵称
	 */
	private String accountNickname;

	/**
	 * 手机号
	 */
	@Desensitize(type = MOBILE_PHONE)
	private String accountPhoneNumber;

	/**
	 * 账号登录名
	 */
	private String accountUsername;

	/**
	 * 账号邮箱
	 */
	@Desensitize(type = EMAIL)
	private String accountEmail;

	/**
	 * 账号锁定状态
	 */
	private Boolean accountLocked;
	/**
	 * 账号启用状态
	 */
	private Boolean accountEnabled;
	// account field

}
