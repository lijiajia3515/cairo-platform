package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 端点修改状态参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyEndpointStatusArgs implements Serializable {

	/**
	 * logId
	 */
	@NotNull
	@NotBlank
	private String id;


	/**
	 * 状态
	 */
	@NotNull
	private Boolean enabled;
}
