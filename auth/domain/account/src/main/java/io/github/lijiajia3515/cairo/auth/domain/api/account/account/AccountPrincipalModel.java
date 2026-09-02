package io.github.lijiajia3515.cairo.auth.domain.api.account.account;

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

import static io.github.lijiajia3515.cairo.jackson.desensitize.DesensitizeType.MOBILE_PHONE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AccountPrincipalModel implements Serializable {

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
	 * 客户端id
	 */
	private String clientId;

	/**
	 * 账号id
	 */
	private String accountId;
	/**
	 * 手机号
	 */
	@Desensitize(type = MOBILE_PHONE)
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
	 * 昵称
	 */
	private String nickname;

	/**
	 * 头像
	 */
	private String avatarUrl;

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
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 锁定状态
	 */
	private Boolean locked;
}
