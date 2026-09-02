package io.github.lijiajia3515.cairo.auth.domain.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * basic client
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicClient implements Serializable {
	/**
	 * id
	 */
	private String id;
	/**
	 * app id
	 */
	private String appId;
	/**
	 * app endpoint id
	 */
	private String endpointId;
	/**
	 * client id
	 */
	private String clientId;
	/**
	 * client name
	 */
	private String clientName;
}
