package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetWxmpProviderArgs extends AbstractPage<GetWxmpProviderArgs> {
	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 微信公众号连接ids
	 */
	private List<String> wxmpProviderIds;

	/**
	 * 是否启用
	 */
	private Boolean enabled;
}
