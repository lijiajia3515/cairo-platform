package io.github.lijiajia3515.cairo.auth.modules.biz_log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务日志注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BizLog {

	/**
	 * 业务ID
	 *
	 * @return 业务ID
	 */
	String bizId();

	/**
	 * 范围
	 *
	 * @return 范围
	 */
	String scope();

	/*参数
	 */
	Param[] params() default {};

	/**
	 * 参数
	 */
	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface Param {

		/**
		 * key值
		 *
		 * @return key值
		 */
		String key();

		/**
		 * spel 表达式
		 *
		 * @return 表达式
		 */
		String value();
	}
}
