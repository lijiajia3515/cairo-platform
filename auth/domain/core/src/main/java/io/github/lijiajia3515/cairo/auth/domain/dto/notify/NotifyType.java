package io.github.lijiajia3515.cairo.auth.domain.dto.notify;

import lombok.Getter;

import java.util.Arrays;

/**
 * 通知消息类型
 */
@Getter
public enum NotifyType {
	/**
	 * 提醒消息
	 */
	MESSAGE("0"),
	/**
	 * 纯文本消息
	 */
	CONTENT("1"),

	/**
	 * 模板消息
	 */
	TEMPLATE("2")
	;

	/**
	 * 消息类型真值
	 */
	private final String typeValue;

	NotifyType(String typeValue) {
		this.typeValue = typeValue;
	}

	public boolean valid(String typeValue) {
		return Arrays.stream(NotifyType.values()).anyMatch(x->x.typeValue.equals(typeValue));
	}
}
