package io.github.lijiajia3515.cairo.auth.modules.auth_code;

import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeModel;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.NewAuthCodeArgs;

/**
 * 认证码存储接口
 */
public interface AuthCodeStoreService {
	/**
	 * 生成认证code token
	 *
	 * @param args 图形验证码
	 */
	AuthCodeModel generate(NewAuthCodeArgs args);
}
