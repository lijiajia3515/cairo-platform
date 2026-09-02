package io.github.lijiajia3515.cairo.auth.modules.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * url转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlConverter {

	/**
	 * s3协议地址数组
	 */
	public List<String> s3Urls;

	/**
	 * http协议地址数组
	 */
	public List<String> httpUrls;
}
