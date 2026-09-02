package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 业务字典 恢复
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RestoreBizDictArgs implements Serializable {

	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

}
