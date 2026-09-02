package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除应用
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteAppArgs implements Serializable {

	/**
	 * 应用ID
	 */
	@NotNull
	private String appId;
}
