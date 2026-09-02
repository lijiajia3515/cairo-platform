package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除子应用ID
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteSubappArgs implements Serializable {
	/**
	 * 子应用ID
	 */
	@NotNull
	@NotBlank
	private String id;
}
