package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetAppReleaseLatestVersionArgs {
	/**
	 * 应用ID
	 */
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
