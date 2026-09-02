package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg_record;

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
public class GetWxmpTemplateMsgRecordArgs extends AbstractPage<GetWxmpTemplateMsgRecordArgs> {
	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 业务ID
	 */
	private String bizId;


	/**
	 * 结果
	 */
	private Boolean success;
}
