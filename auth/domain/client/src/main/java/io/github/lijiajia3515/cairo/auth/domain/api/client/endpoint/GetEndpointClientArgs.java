package io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint;


import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 终端 查询 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetEndpointClientArgs extends AbstractPage<GetEndpointClientArgs> implements Serializable {

	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 启用状态
	 */
	private Boolean enabled;
}
