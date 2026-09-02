package io.github.lijiajia3515.cairo.auth.domain.api.client.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JsApiTicket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxmpJsApiTicket {
	/**
	 * wxmpAppId
	 */
	private String appId;

	/**
	 * jsapiTicket
	 */
	private String jsapiTicket;


}
