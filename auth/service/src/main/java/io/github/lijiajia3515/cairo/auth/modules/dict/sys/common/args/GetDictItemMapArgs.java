package io.github.lijiajia3515.cairo.auth.modules.dict.sys.common.args;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDictItemMapArgs implements Serializable {
	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

	/**
	 * 字典项ID
	 */
	private List<String> itemIds;
}
