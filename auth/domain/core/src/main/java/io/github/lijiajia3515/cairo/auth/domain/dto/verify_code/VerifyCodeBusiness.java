package io.github.lijiajia3515.cairo.auth.domain.dto.verify_code;


import io.github.lijiajia3515.cairo.core.business.Business;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 默认业务结果
 */
@Getter
@Accessors(fluent = true)
public enum VerifyCodeBusiness implements Business {
	/**
	 * 验证码错误
	 */
	BAD("VerifyCode.Bad", "验证码错误"),

	/**
	 * 验证码失效
	 */
	EXPIRED("VerifyCode.Expired", "验证码已过期");

	public final String code;
	public final String message;


	VerifyCodeBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
