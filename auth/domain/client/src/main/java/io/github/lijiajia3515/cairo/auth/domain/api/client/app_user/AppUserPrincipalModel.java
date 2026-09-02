package io.github.lijiajia3515.cairo.auth.domain.api.client.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.jackson.desensitize.Desensitize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

import static io.github.lijiajia3515.cairo.jackson.desensitize.DesensitizeType.EMAIL;
import static io.github.lijiajia3515.cairo.jackson.desensitize.DesensitizeType.MOBILE_PHONE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AppUserPrincipalModel implements Serializable {
	/**
	 * 唯一id
	 */
	private String id;

	/**
	 * 登录方式
	 */
	private String loginType;

	/**
	 * 应用id
	 */
	private String appId;

	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 客户id
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
	@Desensitize(type = MOBILE_PHONE)
	private String accountPhoneNumber;

	/**
	 * 账号邮箱
	 */
	@Desensitize(type = EMAIL)
	private String accountEmail;

	/**
	 * 头像
	 */
	private String accountAvatarUrl;

	/**
	 * 账号启用状态
	 */
	private Boolean accountEnabled;

	/**
	 * 账号锁定状态
	 */
	private Boolean accountLocked;
	// account field

}
