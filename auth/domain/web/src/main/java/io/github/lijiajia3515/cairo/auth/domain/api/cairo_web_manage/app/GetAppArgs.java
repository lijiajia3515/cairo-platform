package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app;


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
public class GetAppArgs extends AbstractPage<GetAppArgs> implements Serializable {

	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 范围
	 */
	private List<String> scopes;

	/**
	 * 私有应用
	 */
	private Boolean privateApp;

	/**
	 * 启用状态
	 */
	private Boolean enabled;
}
