package io.github.lijiajia3515.cairo.auth.domain.dto.link;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 短链
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataLink {
	private String linkId;
	/**
	 * 短链地址
	 */
	private String shortUrl;

	/**
	 * 链接地址
	 */
	private String linkUrl;

	/**
	 * 状态
	 */
	private boolean enabled;

	/**
	 * 访问次数
	 */
	private int accessCount;


	/**
	 * 最后访问时间
	 */
	private LocalDateTime lastAccessTime;
	/**
	 * 元信息
	 */
	private CairoAppUserMetadata metadata;
}
