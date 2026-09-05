package io.github.lijiajia3515.cairo.auth.domain.api.app_user.subapp_version;


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
public class GetSubappVersionArgs extends AbstractPage<GetSubappVersionArgs> implements Serializable {

	/**
	 * 子应用id
	 */
	private String subappId;

	/**
	 * 启用/禁用
	 */
	private Boolean enabled;
}
