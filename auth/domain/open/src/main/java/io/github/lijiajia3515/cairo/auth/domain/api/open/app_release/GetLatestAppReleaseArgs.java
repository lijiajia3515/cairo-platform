package io.github.lijiajia3515.cairo.auth.domain.api.open.app_release;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class GetLatestAppReleaseArgs {
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

}
