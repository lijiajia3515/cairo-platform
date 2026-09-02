package io.github.lijiajia3515.cairo.auth.domain.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CairoFileItem {
	/**
	 * 名称
	 */
	private String name;

	/**
	 * 文件路径
	 */
	private String key;
	/**
	 * 文件key
	 */
	private String s3Url;

	/**
	 * 预览下载key
	 */
	private String httpUrl;
	/**
	 * 文件名
	 */
	private String fileName;

	/**
	 * 文件大小（字节）
	 */
	private Long size;

	/**
	 * etag
	 */
	private String etag;

	/**
	 * 文件版本
	 */
	private String version;
	/**
	 * 是否最新
	 */
	private boolean isLatest;

	/**
	 * 最后修改时间
	 */
	private LocalDateTime lastModifiedDate;

	/**
	 * 文件属性
	 */
	private Map<String, String> userMetadata;

	/**
	 * 是否文件夹
	 */
	private boolean dir;
}
