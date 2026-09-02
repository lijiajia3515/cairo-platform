package io.github.lijiajia3515.cairo.auth.domain.api.open.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 注销账号
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class LogoffAccountArgs implements Serializable {


	/**
	 * 手机号
	 */
	@NotNull
	@NotBlank
	@Size(min = 11, max = 20)
	private String phoneNumber;

	/**
	 * 验证码
	 */
	@NotNull
	private String verifyCode;
}
