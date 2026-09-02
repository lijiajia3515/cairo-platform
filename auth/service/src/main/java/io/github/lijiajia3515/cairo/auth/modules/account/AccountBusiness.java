package io.github.lijiajia3515.cairo.auth.modules.account;


import io.github.lijiajia3515.cairo.core.business.Business;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * account business
 */
@Getter
@Accessors(fluent = true)
public enum AccountBusiness implements Business {
	/**
	 * 手机号存在
	 */
	PHONE_NUMBER_EXISTS("Account.PhoneNumberExists", "手机号已存在"),

	/**
	 * 账号不存在
	 */
	NOT_FOUND("Account.NotFound", "账号不存在")

	;
	public final String code;
	public final String message;


	AccountBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
