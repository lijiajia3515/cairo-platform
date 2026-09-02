package io.github.lijiajia3515.cairo.auth.domain.api.client.verify_code;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;


/**
 * 发送短信验证码 参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SendAccountPhoneNumberVerifyCodeArgs {

	@NotNull
	@NotBlank
	private String phoneNumber;

}
