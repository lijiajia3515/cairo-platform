package io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetSingleBizDictInfoArgs implements Serializable {
	private Set<Dict> dicts;

	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@SuperBuilder(toBuilder = true)
	public static class Dict {
		@NotNull
		private String dictId;
		private Set<String> itemIds;
	}
}
