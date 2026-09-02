package io.github.lijiajia3515.cairo.jackson.desensitize;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)  // 针对成员属性进行脱敏
@JacksonAnnotationsInside  // 表示和其他Jackson注解联合使用，如果缺少则无法执行数据脱敏流程
@JsonSerialize(using = DesensitizeJsonSerializer.class)  // 表明使用的序列化的类，定义在后面
public @interface Desensitize {

	/**
	 * 对数据的脱敏策略
	 *
	 * @return 脱敏策略
	 */
	DesensitizeType type();
}
