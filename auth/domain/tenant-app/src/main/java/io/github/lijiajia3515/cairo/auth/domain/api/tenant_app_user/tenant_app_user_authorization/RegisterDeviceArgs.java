package io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user_authorization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册设备参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDeviceArgs {
	@NotNull
	@NotBlank
	private String deviceId;
}
