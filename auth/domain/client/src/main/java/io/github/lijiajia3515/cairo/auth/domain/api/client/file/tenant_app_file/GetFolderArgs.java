package io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetFolderArgs {
	/**
	 * 企业ID
	 */
	@NotNull
	private String tenantId;
	/**
	 * 文件前缀
	 */
	private String keyPrefix;
	/**
	 * 是否递归
	 */
	private boolean recursive;

}
