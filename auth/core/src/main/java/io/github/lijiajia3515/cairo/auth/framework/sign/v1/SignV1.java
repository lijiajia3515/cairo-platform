package io.github.lijiajia3515.cairo.auth.framework.sign.v1;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口签名注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SignV1 {
	/**
	 * 允许时间差的值(历史值)
	 *
	 * @return 秒数
	 */
	int beforeSeconds() default 300;

	/**
	 * 允许时间差值(未来值)
	 *
	 * @return 秒数
	 */
	int afterSeconds() default 300;

	/**
	 * 幂等属性
	 */
	SignIdempotent idempotent() default SignIdempotent.REQUIRED;
}
