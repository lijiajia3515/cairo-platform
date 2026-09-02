package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag;

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
public class ModifyUserTagStatusArgs implements Serializable {

	/**
	 * tagId
	 */
	@NotNull
	private String tagId;

	/**
	 * 启用状态
	 */
	@NotNull
	private Boolean enabled;
}
