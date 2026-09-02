package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyAppReleaseInfoArgs {


	/**
	 * 应用ID
	 */
	@NotNull
	@NotBlank
	private String appId;

	/**
	 * 终端ID
	 */
	@NotNull
	@NotBlank
	private String endpointId;

	/**
	 * 类型
	 */
	@NotNull
	@NotBlank
	private String type;

	/**
	 * 版本
	 */
	@NotNull
	@NotBlank
	private String appVersion;


	/**
	 * 是否发行版本，true-是，否-预览版本
	 */
	private Boolean releaseVersion;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 描述/备注
	 */
	private String remark;

	/**
	 * 是否强制更新
	 */
	private Boolean force;


	/**
	 * 网页访问地址
	 */
	private String webUrl;

	/**
	 * 安卓安装包下载地址
	 */
	private String androidApkUrl;

	/**
	 * ios应用商店跳转地址
	 */
	private String iosAppStoreUrl;
}
