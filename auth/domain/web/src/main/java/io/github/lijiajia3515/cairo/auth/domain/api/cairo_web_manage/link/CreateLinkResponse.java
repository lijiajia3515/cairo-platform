package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建链接返回值
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLinkResponse {
	/**
	 * 短链ID
	 */
	private String linkId;

	/**
	 * 链接URL
	 */
	private String linkUrl;
	/**
	 * 短链URL
	 */
	private String shortUrl;
}
