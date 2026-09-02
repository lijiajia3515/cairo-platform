package io.github.lijiajia3515.cairo.auth.domain.api.client.dict.biz;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetBizDictInfoArgs implements Serializable {
	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;
}
