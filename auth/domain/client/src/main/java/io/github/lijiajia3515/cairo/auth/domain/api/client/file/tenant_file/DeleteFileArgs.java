package io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 删除文件
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
	 * 文件前缀
	 */
	@NotNull
	@Builder.Default
	private String keyPrefix = "";

	/**
	 * s3协议地址数组
	 */
	public List<String> s3Urls;

	/**
	 * http协议地址数组
	 */
	public List<String> httpUrls;
}
