package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 获取文件下载地址
 * 返回值：
 * {
 *   "code": 0,
 *   "data": {
 *     "url": "https://foo.bar.com/files/9/180"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeFileDownloadResponse {
	/**
	 * 文档下载地址
	 */
	private String url;
	/**
	 * 文档校验和 (checksum)
	 */
	private String digest;
	/**
	 * 文档校验和算法 md5 或者 sha1
	 */
	private String digestType;

	/**
	 * 请求文档下载地址所需要的额外请求头，例如某些云存储商会要求额外的签名头等
	 */
	private Map<String, String> headers;
}
