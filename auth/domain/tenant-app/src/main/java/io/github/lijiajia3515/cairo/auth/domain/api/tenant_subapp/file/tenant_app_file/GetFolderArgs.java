package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file;

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
	 * 文件前缀
	 */
	private String keyPrefix;
	/**
	 * 是否递归
	 */
	private boolean recursive;

}
