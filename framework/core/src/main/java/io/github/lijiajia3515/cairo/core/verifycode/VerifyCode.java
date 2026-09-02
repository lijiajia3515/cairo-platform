package io.github.lijiajia3515.cairo.core.verifycode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCode {
	/**
	 * 目标
	 */
	private String target;

	/**
	 * 验证码
	 */
	private String verifyCode;

	/**
	 * 失败次数
	 */
	private int failCount;

	/**
	 * 过期时间
	 */
	private boolean expired;

	/**
	 * 过期时间
	 */
	private LocalDateTime expiredTime;
}
