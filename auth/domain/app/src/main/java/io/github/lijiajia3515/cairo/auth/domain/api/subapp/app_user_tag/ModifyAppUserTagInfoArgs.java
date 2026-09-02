package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAppUserTagInfoArgs implements Serializable {

	/**
	 * tagId
	 */
	@NotNull
	private String tagId;

	/**
	 * 名称
	 */
	private String tagName;
}
