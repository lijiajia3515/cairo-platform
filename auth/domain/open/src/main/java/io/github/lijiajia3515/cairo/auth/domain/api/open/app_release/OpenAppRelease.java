package io.github.lijiajia3515.cairo.auth.domain.api.open.app_release;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAppRelease implements Serializable {

	/**
	 * 终端
	 */
	private String endpointId;

	/**
	 * 终端名称
	 */
	private String endpointName;

	/**
	 * 终端图标
	 */
	private String endpointIcon;

	/**
	 * 类型ID
	 */
	private String typeId;

	/**
	 * 类型名称
	 */
	private String typeName;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 应用版本
	 */
	private String appVersion;

	/**
	 * 是否发行版本，true-是，否-预览版本
	 */
	private Boolean releaseVersion;

	/**
	 * 是否为最新版本
	 */
	private Boolean latestVersion;

	/**
	 * 描述
	 */
	private String remark;

	/**
	 * 是否强制更新
	 */
	private Boolean force;

	/**
	 * 网页端
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

	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;
}
