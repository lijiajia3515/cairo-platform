package io.github.lijiajia3515.cairo.auth.modules.verify_code;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreVerifyCodeArgs {
	/**
	 * 业务码
	 */
	private String bizCode;
	/**
	 * 目标
	 */
	@NotNull
	private String target;

	/**
	 * 验证码
	 */
	@NotNull
	private String verifyCode;

	/**
	 * 过期时间
	 */
	@Builder.Default
	private Duration tll = Duration.ofMinutes(5);
}
