package io.github.lijiajia3515.cairo.auth.domain.dto.app_release;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 应用发行类型
 */
@Getter
public enum AppReleaseType {

	/**
	 * web端
	 */
	WEB("web"),

	/**
	 * 安卓
	 */
	ANDROID("android"),

	/**
	 * ios
	 */
	IOS("ios"),
	;

	/**
	 * 类型值
	 */
	public final String typeValue;

	AppReleaseType(String typeValue) {
		this.typeValue = typeValue;
	}

	/**
	 * 查找 应用发行类型
	 *
	 * @param typeValue 类型值
	 * @return 应用发行类型
	 */
	public static Optional<AppReleaseType> typeValueOf(String typeValue) {
		return Arrays.stream(AppReleaseType.values()).filter(x -> x.typeValue.equals(typeValue)).findFirst();
	}
}
