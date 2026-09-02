package io.github.lijiajia3515.cairo.auth.domain.api.subapp.subapp;


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
 * 查询 子应用  参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetSubappArgs extends AbstractPage<GetSubappArgs> implements Serializable {
	/**
	 * 终端id
	 */
	private String endpointId;


	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 关键字
	 */
	private String keyword;
}
