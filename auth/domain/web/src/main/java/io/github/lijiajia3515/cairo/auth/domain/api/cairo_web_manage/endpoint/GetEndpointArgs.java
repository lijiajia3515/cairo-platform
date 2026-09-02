package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint;


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
 * 应用 查询 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetEndpointArgs extends AbstractPage<GetEndpointArgs> implements Serializable {

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 类型
	 */
	private List<String> typeIds;

	/**
	 * 范围
	 */
	private List<String> scopeIds;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 关键字
	 */
	private String keyword;
}
