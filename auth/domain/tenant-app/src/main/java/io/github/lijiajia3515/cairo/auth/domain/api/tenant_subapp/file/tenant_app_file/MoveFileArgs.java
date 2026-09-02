package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveFileArgs {
	/**
	 * 源路径
	 */
	@NotNull
	private String sourcePath;

	/**
	 * 目标路径
	 */
	@NotNull
	private String targetPath;

	/**
     * 是否文件夹，是：移动文件夹，否：重命名文件，移动文件
	 */
	private boolean dir;
}
