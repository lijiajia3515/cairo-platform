package io.github.lijiajia3515.cairo.auth.framework.security.cairo_security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CairoSecurityProperties {
	/**
	 * cairo app id
	 */
	@Builder.Default
	private String cairoAppId = "cairo";

	@Builder.Default
	private String endpointId = "web";

	@Builder.Default
	private String manageSubappId = "manage";

	/**
	 * portal app id
	 */
	@Builder.Default
	private String portalAppId = "portal";
}
