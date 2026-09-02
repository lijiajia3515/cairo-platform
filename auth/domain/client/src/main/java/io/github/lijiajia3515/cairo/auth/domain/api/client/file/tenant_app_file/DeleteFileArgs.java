package io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 删除文件参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteFileArgs {

	/**
	 * 企业ID
	 */
	@NotNull
	private String tenantId;

	/**
	 * 文件夹地址
	 */
	private List<String> folderPaths;

	/**
	 * 文件地址（不携带，tenantId,appId）
	 */
	private List<String> filePaths;

	/**
	 * s3协议地址
	 */
	private List<String> s3Urls;

	/**
	 * http协议地址
	 */
	private List<String> httpUrls;
}
