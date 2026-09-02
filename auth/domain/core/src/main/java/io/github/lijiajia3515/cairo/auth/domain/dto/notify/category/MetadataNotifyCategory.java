package io.github.lijiajia3515.cairo.auth.domain.dto.notify.category;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
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
public class MetadataNotifyCategory {

	/**
	 * 标识
	 */
	private String categoryId;

	/**
	 * 名称
	 */
	private String categoryName;

	/**
	 * 图标
	 */
	private String categoryIcon;

	/**
	 * 状态
	 */
	private Boolean enabled;

	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;

}
