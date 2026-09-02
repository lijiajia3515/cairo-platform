package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 创建用户标签 参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateAppUserTagArgs implements Serializable {

	/**
	 * tagId
	 */
	@NotNull
	private String tagId;

	/**
	 * tag名称
	 */
	@NotNull
	private String tagName;
}
