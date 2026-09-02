package io.github.lijiajia3515.cairo.auth.domain.dto.endpoint;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 终端类型
 */
@Getter
public enum EndpointType {

	/**
	 * 网页
	 */
	WEB("web"),

	/**
	 * 通用手机应用
	 */
	UNIVERSAL_APP("universal_app"),

	/**
	 * 安卓
	 */
	ANDROID("android"),

	/**
	 * IOS
	 */
	IOS("ios"),

	/**
	 * h5端
	 */
	H5("h5"),

	/**
	 * 小程序
	 */
	MINI_APP("mini_app"),

	/**
	 * h5端
	 */
	BIZ("biz"),
	;

	/**
	 * 类型值
	 */
	public final String typeValue;

	EndpointType(String typeValue) {
		this.typeValue = typeValue;
	}

	/**
	 * 查找 终端类型
	 *
	 * @param typeValue 类型值
	 * @return 终端类型
	 */
	public static Optional<EndpointType> typeValueOf(String typeValue) {
		return Arrays.stream(EndpointType.values()).filter(x -> x.typeValue.equals(typeValue)).findFirst();
	}
}
