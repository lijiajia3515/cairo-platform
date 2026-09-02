package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MkdirArgs implements Serializable {

	/**
	 * 文件夹地址(单个）
	 */
	private String dirPath;

	/**
	 * 文件夹地址(多个）
	 */
	private List<String> dirPaths;

	/**
	 * 文件属性
	 */
	private Map<String, String> userMetadata;
}
