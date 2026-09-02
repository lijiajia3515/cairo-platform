package io.github.lijiajia3515.cairo.gateway.framework.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * redis properties
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CairoRedisProperties {

	/**
	 * key前缀
	 */
	@Builder.Default
	private String keyPrefix = "";
}
