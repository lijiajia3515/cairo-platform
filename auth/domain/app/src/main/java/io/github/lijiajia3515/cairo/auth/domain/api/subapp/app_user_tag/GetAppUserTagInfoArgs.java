package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAppUserTagInfoArgs implements Serializable {
	/**
	 * 标签ID
	 */
	@NotNull
	private String tagId;
}
