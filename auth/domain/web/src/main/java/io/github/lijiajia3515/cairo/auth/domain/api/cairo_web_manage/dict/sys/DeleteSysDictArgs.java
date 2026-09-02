package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * dict delete request
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteSysDictArgs implements Serializable {

	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

}
