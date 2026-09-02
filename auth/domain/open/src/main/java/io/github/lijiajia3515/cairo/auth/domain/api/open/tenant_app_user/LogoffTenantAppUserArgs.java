package io.github.lijiajia3515.cairo.auth.domain.api.open.tenant_app_user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 注销用户参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class LogoffTenantAppUserArgs implements Serializable {
	/**
	 * tenantId
	 */
	@NotNull
	@NotBlank
	private String tenantId;

	/**
	 * appId
	 */
	@NotNull
	@NotBlank
	private String appId;

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
