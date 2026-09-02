package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 移动区域参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveAreaArgs implements Serializable {

	/**
	 * 移动区域ID1
	 */
	@NotNull
	private String moveAreaId1;

	/**
	 * 移动区域ID2
	 */
	@NotNull
	private String moveAreaId2;

}
