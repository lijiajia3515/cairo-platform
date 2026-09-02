package io.github.lijiajia3515.cairo.auth.framework.security.cairo_security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义认证注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CairoSecurity {

	/**
	 * 用户类型
	 *
	 * @return 用户类型
	 */
	CairoSecurityType type();
}
