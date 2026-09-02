package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user;


import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum TenantAppUserExtension implements Extension<TenantAppUserField> {
	/**
	 * 基础信息
	 */
	BASIC(TenantAppUserField.NICKNAME, TenantAppUserField.ACCOUNT_AVATAR_URL),
	/**
	 * 基本信息
	 */
	INFO(TenantAppUserField.NICKNAME, TenantAppUserField.ACCOUNT_AVATAR_URL, TenantAppUserField.PHONE_NUMBER, TenantAppUserField.USER_STATUS),
	/**
	 * 完整信息
	 */
	FULL_INFO(
		TenantAppUserField.NICKNAME,
		TenantAppUserField.PHONE_NUMBER,
		TenantAppUserField.ROLE,
		TenantAppUserField.DEPARTMENT,
		TenantAppUserField.TAG,
		TenantAppUserField.USER_STATUS,

		TenantAppUserField.ACCOUNT_NICKNAME,
		TenantAppUserField.ACCOUNT_AVATAR_URL,
		TenantAppUserField.ACCOUNT_USERNAME,
		TenantAppUserField.ACCOUNT_PHONE_NUMBER,
		TenantAppUserField.ACCOUNT_EMAIL
	),
	ALL(TenantAppUserField.values());
	private final Set<TenantAppUserField> fields;

	TenantAppUserExtension(TenantAppUserField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<TenantAppUserField> fields() {
		return fields;
	}
}
