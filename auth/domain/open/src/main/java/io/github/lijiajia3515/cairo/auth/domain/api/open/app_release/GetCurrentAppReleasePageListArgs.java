package io.github.lijiajia3515.cairo.auth.domain.api.open.app_release;


import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCurrentAppReleasePageListArgs extends AbstractPage<GetCurrentAppReleasePageListArgs> {
	/**
	 * 应用ID
	 */
	@NotNull
	@NotBlank
	private String appId;

	/**
	 * 终端ID
	 */
	@NotNull
	@NotBlank
	private String endpointId;

	/**
	 * 类型
	 */
	@NotNull
	@NotBlank
	private String type;

	/**
	 * 是否正式版本
	 */
	@Builder.Default
	private boolean releaseVersion = true;
}
