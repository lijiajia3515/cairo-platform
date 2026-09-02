package io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 字典类型
 */
@Getter
public enum DictType {

	/**
	 * 系统
	 */
	SYSTEM("system"),

	/**
	 * 业务级
	 */
	BIZ_TEMPLATE("biz_template"),
	;

	/**
	 * 类型值
	 */
	public final String typeValue;

	DictType(String typeValue) {
		this.typeValue = typeValue;
	}

	/**
	 * 查找 字典类型
	 *
	 * @param typeValue 类型值
	 * @return 字典类型
	 */
	public static Optional<DictType> typeValueOf(String typeValue) {
		return Arrays.stream(DictType.values()).filter(x -> x.typeValue.equals(typeValue)).findFirst();
	}
}
