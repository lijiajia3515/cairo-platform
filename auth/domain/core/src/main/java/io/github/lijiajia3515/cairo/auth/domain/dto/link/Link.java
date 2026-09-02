package io.github.lijiajia3515.cairo.auth.domain.dto.link;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Link {
	private String linkId;
	/**
	 * 短链地址
	 */
	private String shortUrl;

	/**
	 * 链接地址
	 */
	private String linkUrl;
}
