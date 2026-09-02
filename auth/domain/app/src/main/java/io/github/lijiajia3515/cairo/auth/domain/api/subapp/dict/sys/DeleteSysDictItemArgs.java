package io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * dict delete
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteSysDictItemArgs implements Serializable {

	/**
	 * 字典iD
	 */
	@NotNull
	@NotEmpty
	private String dictId;

	/**
	 * 字典项ID
	 */
	@NotNull
	@NotEmpty
	private String itemId;
}
