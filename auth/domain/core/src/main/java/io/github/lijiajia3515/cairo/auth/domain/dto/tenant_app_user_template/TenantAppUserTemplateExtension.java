package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template;


import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum TenantAppUserTemplateExtension implements Extension<TenantAppUserTemplateField> {
	/**
	 * 基础信息
	 */
	BASIC(TenantAppUserTemplateField.NICKNAME, TenantAppUserTemplateField.ACCOUNT_AVATAR_URL),
	/**
	 * 基本信息
	 */
	INFO(TenantAppUserTemplateField.NICKNAME, TenantAppUserTemplateField.ACCOUNT_AVATAR_URL, TenantAppUserTemplateField.PHONE_NUMBER, TenantAppUserTemplateField.USER_STATUS),
	/**
	 * 完整信息
	 */
	FULL_INFO(
		TenantAppUserTemplateField.NICKNAME,
		TenantAppUserTemplateField.PHONE_NUMBER,
		TenantAppUserTemplateField.ROLE,
		TenantAppUserTemplateField.DEPARTMENT,
		TenantAppUserTemplateField.USER_STATUS,

		TenantAppUserTemplateField.ACCOUNT_NICKNAME,
		TenantAppUserTemplateField.ACCOUNT_AVATAR_URL,
		TenantAppUserTemplateField.ACCOUNT_USERNAME,
		TenantAppUserTemplateField.ACCOUNT_PHONE_NUMBER,
		TenantAppUserTemplateField.ACCOUNT_EMAIL
	),
	ALL(TenantAppUserTemplateField.values());
	private final Set<TenantAppUserTemplateField> fields;

	TenantAppUserTemplateExtension(TenantAppUserTemplateField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<TenantAppUserTemplateField> fields() {
		return fields;
	}
}
