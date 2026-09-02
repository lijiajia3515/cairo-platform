package io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
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
public class GetSysDictArgs extends AbstractPage<GetSysDictArgs> {

	/**
	 * 字典ID列表
	 */
	private Set<String> dictIds;

	/**
	 * 关键字
	 */
	private String keyword;
}
