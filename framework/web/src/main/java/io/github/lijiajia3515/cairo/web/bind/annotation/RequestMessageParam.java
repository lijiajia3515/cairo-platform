package io.github.lijiajia3515.cairo.web.bind.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMessageParam {
	/**
	 * 是否必须
	 *
	 * @return required
	 */
	boolean required() default true;

	/**
	 * 参数名称
	 *
	 * @return 名称
	 */
	String name() default "Param";
}
