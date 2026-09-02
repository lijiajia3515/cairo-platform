package io.github.lijiajia3515.cairo.auth.modules.sns;


import io.github.lijiajia3515.cairo.core.business.Business;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 第三方认证业务码定义
 */
@Getter
@Accessors(fluent = true)
public enum SnsBusiness implements Business {
	/**
	 * 第三方认证授权码错误
	 */
	BAD("Sns.SnsCodeBad", "授权码错误");

	public final String code;
	public final String message;


	SnsBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
