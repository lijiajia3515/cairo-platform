package io.github.lijiajia3515.cairo.auth.domain.api.client.subapp;


import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 子应用查询 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetSubappClientArgs extends AbstractPage<GetSubappClientArgs> implements Serializable {

	/**
	 * 应用ID
	 */
	private String appId;
	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 启用状态
	 */
	private Boolean enabled;


}
