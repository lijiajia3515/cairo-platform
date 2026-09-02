package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category;


import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetNotifyCategoryArgs extends AbstractPage<GetNotifyCategoryArgs> implements Serializable {

	/**
	 * 标识
	 */
	private List<String> categoryIds;


	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 状态
	 */
	private Boolean enabled;
}
