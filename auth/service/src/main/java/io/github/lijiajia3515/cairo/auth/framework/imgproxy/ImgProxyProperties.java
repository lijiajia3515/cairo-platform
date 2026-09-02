package io.github.lijiajia3515.cairo.auth.framework.imgproxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ImgProxyProperties {
	/**
	 * 服务器
	 */
	private String server;

	/**
	 * key
	 */
	private String key;

	/**
	 * 盐
	 */
	private String slat;

}
