package io.github.lijiajia3515.cairo.auth.domain.dto.app_user;


import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum AppUserExtension implements Extension<AppUserField> {
	/**
	 * 基础信息
	 */
	BASIC(AppUserField.NICKNAME, AppUserField.ACCOUNT_AVATAR_URL),
	/**
	 * 基本信息
	 */
	INFO(AppUserField.NICKNAME, AppUserField.ACCOUNT_AVATAR_URL, AppUserField.PHONE_NUMBER, AppUserField.USER_STATUS),
	/**
	 * 完整信息
	 */
	FULL_INFO(
		AppUserField.NICKNAME,
		AppUserField.PHONE_NUMBER,
		AppUserField.ROLE,
		AppUserField.DEPARTMENT,
		AppUserField.TAG,
		AppUserField.USER_STATUS,

		AppUserField.ACCOUNT_NICKNAME,
		AppUserField.ACCOUNT_AVATAR_URL,
		AppUserField.ACCOUNT_USERNAME,
		AppUserField.ACCOUNT_PHONE_NUMBER,
		AppUserField.ACCOUNT_EMAIL
	),
	ALL(AppUserField.values());
	private final Set<AppUserField> fields;

	AppUserExtension(AppUserField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<AppUserField> fields() {
		return fields;
	}
}
