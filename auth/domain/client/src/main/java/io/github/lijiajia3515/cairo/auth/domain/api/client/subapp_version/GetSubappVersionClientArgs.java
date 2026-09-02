package io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_version;


import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 子应用版本 查询 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetSubappVersionClientArgs extends AbstractPage<GetSubappVersionClientArgs> implements Serializable {
	/**
	 * 子应用ID
	 */
	private String subappId;


	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 关键字
	 */
	private String keyword;
}
