package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 修改短链状态
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteLinkArgs implements Serializable {

	/**
	 * 短链ID
	 */
	@NotNull
	private String linkId;

}
