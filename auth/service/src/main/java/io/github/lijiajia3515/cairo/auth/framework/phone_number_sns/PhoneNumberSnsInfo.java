package io.github.lijiajia3515.cairo.auth.framework.phone_number_sns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 手机信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneNumberSnsInfo {
	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 区号
	 */
	private String countryCode;

	/**
	 * 时间
	 */
	private LocalDateTime time;
}
