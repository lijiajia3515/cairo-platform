package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetBizDictItemPageInfoArgs extends AbstractPage<GetBizDictItemPageInfoArgs> implements Serializable{
	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

	/**
	 * 父级ID
	 */
	@NotNull
	private String parentItemId;
}
