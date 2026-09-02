package io.github.lijiajia3515.cairo.auth.modules.captcha.token;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 验证注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface VerifyCaptchaToken {

	/**
	 * 名称
	 *
	 * @return 名称
	 */
	String name() default "";

	/**
	 * 允许调用失败最大次数
	 */
	int maxFailCount() default 1;
}
