package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication;

import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
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
public class CairoOAuthAccountPrincipal implements Serializable {

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
	 * 应用id
	 */
	private String appId;

	/**
	 * 客户端id
	 */
	private String clientId;

	/**
	 * 账号id
	 */
	private String accountId;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 昵称
	 */
	private String nickname;

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
	private String email;

	/**
	 * 角色
	 */
	private List<CairoRole> roles;

	/**
	 * 部门
	 */
	private List<CairoDepartment> departments;

	/**
	 * 标签
	 */
	private List<CairoTag> tags;

	/**
	 * 账号锁定状态
	 */
	private Boolean locked;

	/**
	 * 启用状态
	 */
	private Boolean enabled;
}
