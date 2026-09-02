package io.github.lijiajia3515.cairo.auth.framework.sns;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 第三方账号厂家
 */
@Getter
public enum SnsPartner {
	DEFAULT("default", "默认", "第三方用户"),
	WX("wx", "微信", "微信用户");

	/**
	 * 类型名称
	 */
	private final String partnerId;
	private final String name;
	private final String nickname;

	SnsPartner(String partnerId, String name, String nickname) {
		this.partnerId = partnerId;
		this.name = name;
		this.nickname = nickname;
	}

	public static Optional<SnsPartner> partnerIdOf(String partnerValue) {
		return Arrays.stream(values()).filter(x -> x.partnerId.equals(partnerValue)).findFirst();
	}

	public static SnsPartner typeValueOf(String type) {
		return SnsType.typeValueOf(type).map(SnsType::getChannel).orElse(SnsPartner.DEFAULT);
	}

}
