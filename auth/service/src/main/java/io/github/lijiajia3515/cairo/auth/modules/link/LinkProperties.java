package io.github.lijiajia3515.cairo.auth.modules.link;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkProperties {
	/**
	 * 默认跳转路径
	 */
	@Builder.Default
	private String defaultUrl = "https://www.cairo.com";

	/**
	 * 短链前缀地址
	 */
	@Builder.Default
	private String shortUrlPrefix = "https://s.cairo.com/";
}
