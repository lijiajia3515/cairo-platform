package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 获取第三方认证类型
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetProviderTypeArgs extends AbstractPage<GetProviderTypeArgs> implements Serializable {


	/**
	 * 启用状态
	 */
	@Builder.Default
	private Boolean enabled = true;
}
