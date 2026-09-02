package io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopySysDictByDictIdArgs implements Serializable {
	/**
	 * 当前字典ID
	 */
	@NotBlank
	private String currentDictId;

	/**
	 * 新字典id
	 */
	@NotBlank
	private String newDictId;

}
