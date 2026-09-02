package io.github.lijiajia3515.cairo.auth.framework.security.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Getter
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CairoAuthAccount implements OAuth2User, UserDetails {
	/**
	 * id
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
	 * 账号id
	 */
	private String accountId;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 登录名
	 */
	private String loginname;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 启用状态
	 */
	private boolean enabled;

	/**
	 * 锁定状态
	 */
	private boolean locked;

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
		return !locked;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public String getUsername() {
		return accountId;
	}

	@Override
	@JsonIgnore
	public String getName() {
		return accountId;
	}
}
