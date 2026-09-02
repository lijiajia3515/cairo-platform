package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAppReleaseArgs extends AbstractPage<GetAppReleaseArgs> implements Serializable {

	/**
	 * 类型
	 */
	private String type;


	/**
	 * 是否发行版本，true-是，否-预览版本
	 */
	private Boolean releaseVersion;

	/**
	 * 是否为最新版本
	 */
	private Boolean latestVersion;

	/**
	 * 是否强制更新
	 */
	private Boolean force;

}
