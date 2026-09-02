package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetBizDictDetailArgs extends AbstractPage<GetBizDictDetailArgs> {

	/**
	 * 字典ID列表
	 */
	@NotNull
	@NotEmpty
	private Set<String> dictIds;

}
