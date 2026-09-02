package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAreaArgs extends AbstractPage<DeleteAreaArgs> implements Serializable {
	/**
	 * 区域ID
	 */
	@NotNull
	private String areaId;
}
