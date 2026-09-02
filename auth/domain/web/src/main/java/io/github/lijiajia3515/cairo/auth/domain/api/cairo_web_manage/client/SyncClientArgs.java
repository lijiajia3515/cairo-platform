package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncClientArgs {
	/**
	 * 同步前客户端id
	 */
	@NotNull
	private String beforeId;

	/**
	 * 同步后客户端id
	 */
	@NotNull
	private String afterId;

	/**
	 * 同步后客户端版本号
	 */
	@NotNull
	private Long afterVersion;


}
