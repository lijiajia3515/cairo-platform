package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 移动功能权限
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MoveSubappArgs implements Serializable {

	/**
	 * 移动ID1
	 */
	@NotNull
	private String moveId1;

	/**
	 * 移动ID2
	 */
	@NotNull
	private String moveId2;


}
