package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAreaPageListArgs extends AbstractPage<GetAreaPageListArgs> implements Serializable {

	/**
	 * 关键字搜索
	 */
	private String keyword;

	/**
	 * 上级区域ID
	 */
	private String parentAreaId;

	/**
	 * 是否启用
	 */
	private Boolean enabled;

	/**
	 * 是否热门
	 */
	private Boolean hot;

}
