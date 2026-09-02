package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAppReleaseArgs implements Serializable {
	/**
	 * 应用ID
	 */
	@NotNull
	@NotBlank
	private String appId;

	/**
	 * 终端ID
	 */
	@NotBlank
	private String endpointId;

	/**
	 * 类型
	 */
	@NotBlank
	private String type;

	/**
	 * 应用版本
	 */
	@NotBlank
	private String appVersion;
}
