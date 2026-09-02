package io.github.lijiajia3515.cairo.auth.framework.sns;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 第三方认证类型
 */
@Getter
public enum SnsType {

	WX_WEB("wx_web", SnsPartner.WX, "微信开放用户"),
	WX_MP("wx_mp", SnsPartner.WX, "微信公众用户"),
	WX_MA("wx_ma", SnsPartner.WX, "微信小程序用户");;

	/**
	 * 类型名称
	 */
	private final String typeValue;

	/**
	 * 联接渠道
	 */
	public final SnsPartner channel;


	private final String nickname;

	SnsType(String typeValue, SnsPartner channel, String nickname) {
		this.typeValue = typeValue;
		this.channel = channel;
		this.nickname = nickname;
	}

	public static Optional<SnsType> typeValueOf(String typeValue) {
		return Arrays.stream(values()).filter(x -> x.typeValue.equals(typeValue)).findFirst();
	}

}
