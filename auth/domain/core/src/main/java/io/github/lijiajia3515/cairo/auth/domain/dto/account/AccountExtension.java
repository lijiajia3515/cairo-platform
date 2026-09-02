package io.github.lijiajia3515.cairo.auth.domain.dto.account;


import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum AccountExtension implements Extension<AccountField> {
	/**
	 * 基础信息
	 */
	BASIC(AccountField.NICKNAME, AccountField.AVATAR_URL, AccountField.JOIN_TIME),
	/**
	 * 基本信息
	 */
	INFO(AccountField.NICKNAME,
		AccountField.AVATAR_URL,
		AccountField.PHONE_NUMBER,
		AccountField.EMAIL,
		AccountField.STATUS,
		AccountField.JOIN_TIME
	),
	/**
	 * 完整信息
	 */
	FULL_INFO(
		AccountField.NICKNAME,
		AccountField.AVATAR_URL,
		AccountField.PHONE_NUMBER,
		AccountField.USERNAME,
		AccountField.EMAIL,
		AccountField.STATUS,
		AccountField.JOIN_TIME
	),
	ALL(AccountField.values());
	private final Set<AccountField> fields;

	AccountExtension(AccountField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<AccountField> fields() {
		return fields;
	}
}
