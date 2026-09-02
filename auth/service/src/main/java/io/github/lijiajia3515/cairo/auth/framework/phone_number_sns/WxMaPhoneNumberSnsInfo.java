package io.github.lijiajia3515.cairo.auth.framework.phone_number_sns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 联接
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxMaPhoneNumberSnsInfo {
	/**
	 * 用户绑定的手机号(国外手机号会有区号)
	 */
	private String phoneNumber;

	/**
	 * 没有区号的手机号
	 */
	private String prunePhoneNumber;

	/**
	 * 区号
	 */
	private String countryCode;

	/**
	 * long
	 */
	private Long timestamp;
}
