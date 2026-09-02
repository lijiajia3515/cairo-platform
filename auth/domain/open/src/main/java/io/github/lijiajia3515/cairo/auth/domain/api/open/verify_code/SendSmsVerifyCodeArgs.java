package io.github.lijiajia3515.cairo.auth.domain.api.open.verify_code;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendSmsVerifyCodeArgs {
	/**
	 * 手机号
	 */
	@NotNull
	@Size(min = 11, max = 20)
	private String phoneNumber;
}
