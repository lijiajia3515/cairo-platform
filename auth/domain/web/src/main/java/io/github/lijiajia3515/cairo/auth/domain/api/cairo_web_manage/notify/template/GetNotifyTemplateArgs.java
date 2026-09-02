package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 查询通知消息模板 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetNotifyTemplateArgs extends AbstractPage<GetNotifyTemplateArgs> {
	/**
	 * 关键字
	 */
	private String keyword;


	/**
	 * 分类
	 */
	private List<String> categoryIds;

	/**
	 * 消息类型
	 */
	private List<String> messageTypes;

	/**
	 * 跳转方式
	 */
	private List<String> linkTypes;

	/**
	 * 是否启用
	 */
	private Boolean enabled;


}
