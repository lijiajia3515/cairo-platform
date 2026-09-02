package io.github.lijiajia3515.cairo.auth.framework.cairo_auth;

import lombok.Data;

import java.io.Serializable;

/**
 * 配置
 */
@Data
public class AuthProperties implements Serializable {

	/**
	 * 默认头像地址
	 */
	private String defaultAvatarUrl;

	/**
	 * 是否开启自动注册(仅支持手机号验证码模式)
	 */
	private Boolean autoRegister;
}
