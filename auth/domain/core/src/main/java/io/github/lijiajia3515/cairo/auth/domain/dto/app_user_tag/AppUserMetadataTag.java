package io.github.lijiajia3515.cairo.auth.domain.dto.app_user_tag;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;


/**
 * 应用用户标签
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AppUserMetadataTag implements Serializable {
	/**
	 * 标签ID
	 */
	private String tagId;

	/**
	 * 标签名称
	 */
	private String tagName;

	/**
	 * 启用搞状态
	 */
	private Boolean enabled;

	/**
	 * 用户人数
	 */
	private Integer userCount;

	/**
	 * 元信息
	 */
	private CairoAppUserMetadata metadata;
}
