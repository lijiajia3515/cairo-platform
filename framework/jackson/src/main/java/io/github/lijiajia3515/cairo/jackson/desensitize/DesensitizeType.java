package io.github.lijiajia3515.cairo.jackson.desensitize;

import cn.hutool.core.util.DesensitizedUtil;

import java.util.function.Function;

/**
 * 加密枚举类型
 */
public enum DesensitizeType {
	/**
	 * 用户id
	 */
	USER_ID(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.USER_ID)),

	/**
	 * 中文名
	 */
	CHINESE_NAME(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.CHINESE_NAME)),

	/**
	 * 身份证号
	 */
	ID_CARD(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.ID_CARD)),

	/**
	 * 座机号
	 */
	FIXED_PHONE(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.FIXED_PHONE)),

	/**
	 * 手机号
	 */
	MOBILE_PHONE(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.MOBILE_PHONE)),

	/**
	 * 地址
	 */
	ADDRESS(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.ADDRESS)),

	/**
	 * 电子邮件
	 */
	EMAIL(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.EMAIL)),

	/**
	 * 密码
	 */
	PASSWORD(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.PASSWORD)),

	/**
	 * 中国大陆车牌，包含普通车辆、新能源车辆
	 */
	CAR_LICENSE(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.CAR_LICENSE)),

	/**
	 * 银行卡
	 */
	BANK_CARD(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.BANK_CARD)),

	/**
	 * IPv4地址
	 */
	IPV4(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.IPV4)),

	/**
	 * IPv6地址
	 */
	IPV6(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.IPV6)),

	/**
	 * 定义了一个first_mask的规则，只显示第一个字符。
	 */
	FIRST_MASK(x -> DesensitizedUtil.desensitized(x, DesensitizedUtil.DesensitizedType.FIRST_MASK));

	/**
	 * 实现函数
	 */
	private final Function<String, String> desensitizeFun;

	DesensitizeType(Function<String, String> desensitizeFun) {
		this.desensitizeFun = desensitizeFun;
	}

	public String desensitize(String str) {
		return desensitizeFun.apply(str);
	}
}
