package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user;


import io.github.lijiajia3515.cairo.core.extension.Field;

public enum TenantAppUserField implements Field {
	NICKNAME,
	PHONE_NUMBER,
	LOGIN_TIME,
	DEPARTMENT,
	TAG,
	ROLE,
	USER_STATUS,
    POSITION,
	ACCOUNT_ID,
	ACCOUNT_NICKNAME,
	ACCOUNT_USERNAME,
	ACCOUNT_PHONE_NUMBER,
	ACCOUNT_EMAIL,
	ACCOUNT_AVATAR_URL,
	METADATA;

	@Override
	public String field() {
		return name();
	}
}
