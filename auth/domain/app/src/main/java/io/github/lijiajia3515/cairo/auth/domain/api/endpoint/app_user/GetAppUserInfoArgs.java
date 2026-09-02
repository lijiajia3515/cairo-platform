package io.github.lijiajia3515.cairo.auth.domain.api.endpoint.app_user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 根据应用用户ID获取用户信息参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAppUserInfoArgs implements Serializable {

	/**
	 * 应用用户ID
	 */
	@NotNull
	private String userId;
}
