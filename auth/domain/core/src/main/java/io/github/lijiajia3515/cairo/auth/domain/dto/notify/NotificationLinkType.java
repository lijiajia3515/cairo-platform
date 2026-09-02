package io.github.lijiajia3515.cairo.auth.domain.dto.notify;

import lombok.Getter;

import java.util.Arrays;

/**
 * 通知消息类型
 */
@Getter
public enum NotificationLinkType {
	/**
	 * 不跳转
	 */
	MESSAGE("0"),

	/**
	 * 跳转页面
	 */
	PAGE("1"),

	/**
	 * 内部链接
	 */
	INNER_LINK("2"),

	/**
	 * 第三方链接消息
	 */
	THIRD_LINK("3"),
	;

	/**
	 * 消息类型真值
	 */
	private final String typeValue;

	NotificationLinkType(String typeValue) {
		this.typeValue = typeValue;
	}

	public boolean valid(String typeValue) {
		return Arrays.stream(NotificationLinkType.values()).anyMatch(x->x.typeValue.equals(typeValue));
	}
}
