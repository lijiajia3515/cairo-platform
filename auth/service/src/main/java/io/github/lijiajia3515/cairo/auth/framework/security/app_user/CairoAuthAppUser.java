package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CairoAuthAppUser implements UserDetails, OAuth2User {
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
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 客户端id
	 */
	private String clientId;

	/**
	 * 应用用户id
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

	// account field
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
	// account field

	/**
	 * 权限值
	 */
	@Builder.Default
	private Collection<GrantedAuthority> authorities = new HashSet<>();

	@Builder.Default
	private Map<String, Object> attributes = new HashMap<>();


	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return !accountLocked;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return userEnabled && accountEnabled;
	}


	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return accountPassword;
	}

	@Override
	@JsonIgnore
	public String getUsername() {
		return nickname;
	}

	@Override
	@JsonIgnore
	public String getName() {
		return userId;
	}
}
