package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetSmsTemplateArgs extends AbstractPage<GetSmsTemplateArgs> {
	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 是否启用
	 */
	private Boolean enabled;
}
