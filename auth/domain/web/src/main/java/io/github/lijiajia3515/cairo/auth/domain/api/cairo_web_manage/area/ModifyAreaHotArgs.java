package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyAreaHotArgs implements Serializable {
	/**
	 * 区域ID
	 */
	@NotNull
	private String areaId;

    /**
     * 状态
	 */
	private boolean hot;

}
