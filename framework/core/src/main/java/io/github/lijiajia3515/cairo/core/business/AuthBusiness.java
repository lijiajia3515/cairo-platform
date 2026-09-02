package io.github.lijiajia3515.cairo.core.business;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true, fluent = true)
public enum AuthBusiness implements Business {

	/**
	 * 认证错误
	 */
	ERROR("Auth.Error", "认证错误"),

	/**
	 * 凭证必须
	 */
	INVALID_TOKEN("Auth.InvalidToken", "错误凭证"),


	/**
	 * 权限不足
	 */
	DENIED("Auth.Denied", "权限不足");


	private final String code;
	private final String message;

	AuthBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
