package io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetSysDictItemPageInfoArgs  extends AbstractPage<GetSysDictItemPageInfoArgs> implements Serializable{
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
