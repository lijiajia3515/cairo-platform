package io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.jackson.desensitize.Desensitize;
import io.github.lijiajia3515.cairo.jackson.desensitize.DesensitizeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserInfo implements Serializable {
	/**
	 * 唯一id
	 */
	private String id;

	/**
	 * 登录方式
	 */
	private String loginType;

	/**
	 * 第三方认证类型
	 */
	private String snsType;

	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

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
	 * 头像
	 */
	private String accountAvatarUrl;

	/**
	 * 账号昵称
	 */
	private String accountNickname;

	/**
	 * 账号登录名
	 */
	private String accountUsername;

	/**
	 * 手机号
	 */
	@Desensitize(type = DesensitizeType.MOBILE_PHONE)
	private String accountPhoneNumber;

	/**
	 * 账号邮箱
	 */
	@Desensitize(type = DesensitizeType.EMAIL)
	private String accountEmail;
	// account field


}
