package io.github.lijiajia3515.cairo.auth.domain.dto.account;


import io.github.lijiajia3515.cairo.core.extension.Field;

public enum AccountField implements Field {
	AVATAR_URL,

	NICKNAME,

	USERNAME,
	EMAIL,
	PHONE_NUMBER,
	JOIN_TIME,
	STATUS,
	METADATA;

	@Override
	public String field() {
		return name();
	}
}
