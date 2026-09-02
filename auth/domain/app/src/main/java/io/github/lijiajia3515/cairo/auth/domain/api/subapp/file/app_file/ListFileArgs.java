package io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListFileArgs {
	/**
	 * 文件前缀
	 */
	private String keyPrefix;
	/**
	 * 是否递归
	 */
	private boolean recursive;

}
