package io.github.lijiajia3515.cairo.auth.domain.api.endpoint.subapp;


import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 终端类型 查询 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetCurrentSubappArgs extends AbstractPage<GetCurrentSubappArgs> implements Serializable {

	/**
	 *  终端类型
	 */
	private List<String> typeIds;

	/**
	 *  终端范围
	 */
	private List<String> scopeIds;
}
