package io.github.lijiajia3515.cairo.auth.framework.sign.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignProperties {
	/**
	 * debug 模式
	 */
	@Builder.Default
	private boolean debug = false;
}
