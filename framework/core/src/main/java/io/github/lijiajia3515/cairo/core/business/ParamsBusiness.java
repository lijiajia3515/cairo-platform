package io.github.lijiajia3515.cairo.core.business;


import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 参数异常
 */
@Getter
@Accessors(fluent = true)
public enum ParamsBusiness implements Business {

	/**
	 * 参数错误
	 */
	ERROR("Params.Error", "参数错误"),

	/**
	 * 参数校验失败
	 */
	VALIDATION_FAILED("Params.ValidationFailed", "参数校验失败"),

	;

	/**
	 * 业务状态码
	 */
	public final String code;
	/**
	 * 业务状态解释
	 */
	public final String message;


	ParamsBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
