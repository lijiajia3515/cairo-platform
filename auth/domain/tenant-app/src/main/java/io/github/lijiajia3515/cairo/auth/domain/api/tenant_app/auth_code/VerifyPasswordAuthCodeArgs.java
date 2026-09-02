package io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.auth_code;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密码认证授权认证码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPasswordAuthCodeArgs {

	/**
	 * 密码
	 */
	@NotNull(groups = {WebApi.class, WebService.class})
	private String password;

	/**
	 * 请求ip
	 */
	@NotNull(groups = WebService.class)
	private String ip;


	public interface WebApi {

	}

	public interface WebService {

	}

}
