package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization;

import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TenantSubappUserAuthorizationModel {
	/**
	 * 认证状态
	 */
	private String status;

	/**
	 * 错误信息
	 */
	private String errorMessage;

	/**
	 * 授权时间
	 */
	private Instant issuedAt;

	/**
	 * 过期时间
	 */
	private Instant expiresAt;

	/**
	 * 已授权范围
	 */
	private Set<String> authorizedScopes;

	/**
	 * token标识
	 */
	private String tokenId;

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
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 客户端ID
	 */
	private String clientId;

	/**
	 * 子应用ID
	 */
	private String subappId;

	/**
	 * 子应用版本
	 */
	private String subappVersion;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 部位
	 */
	private String position;

	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 用户启用状态
	 */
	private boolean userEnabled;

	/**
	 * 子应用状态
	 */
	private boolean subappStatus;

	/**
	 * 是否管理员
	 */
	private boolean appAdmin;

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


	// 账号字段
	/**
	 * 账号id
	 */
	private String accountId;
	/**
	 * 账号id
	 */
	private String accountNickname;

	/**
	 * 账号登录名
	 */
	private String accountUsername;

	/**
	 * 手机号
	 */
	private String accountPhoneNumber;

	/**
	 * 账号密码
	 */
	private String accountPassword;

	/**
	 * 账号邮箱
	 */
	private String accountEmail;

	/**
	 * 头像
	 */
	private String accountAvatarUrl;

	/**
	 * 账号启用状态
	 */
	private boolean accountEnabled;

	/**
	 * 账号锁定状态
	 */
	private boolean accountLocked;
	// 账号字段

	/**
	 * 权限
	 */
	private List<String> authorities;
}
