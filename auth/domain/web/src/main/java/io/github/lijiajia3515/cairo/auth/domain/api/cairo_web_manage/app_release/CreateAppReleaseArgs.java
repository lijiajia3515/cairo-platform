package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAppReleaseArgs implements Serializable {
	/**
	 * 应用ID
	 */
	@NotBlank
	private String appId;

	/**
	 * 终端ID
	 */
	@NotBlank
	private String endpointId;

	/**
	 * 类型
	 */
	@NotBlank
	private String type;

	/**
	 * 版本
	 */
	@NotBlank
	private String appVersion;

	/**
	 * 是否发行版本，true-是，否-预览版本
	 */
	@NotNull
	private Boolean releaseVersion;

	/**
	 * 是否为最新版本
	 */
	@NotNull
	private Boolean latestVersion;

	/**
	 * 标题
	 */
	@NotNull
	private String title;


	/**
	 * 描述/备注
	 */
	private String remark;

	/**
	 * 是否强制更新
	 */
	@NotNull
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
