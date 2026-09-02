package io.github.lijiajia3515.cairo.auth.domain.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class FileStat {
	/**
	 * 是否存在
	 */
	private boolean exists;
	/**
	 * 唯一标识
	 */
	private String s3Url;

	/**
	 * bucket
	 */
	private String bucket;

	/**
	 * object
	 */
	private String object;

	/**
	 * 版本
	 */
	private String version;

	/**
	 * 地区
	 */
	private String region;

	/**
	 * etag 类似md5
	 */
	private String etag;

	/**
	 * 最后修改时间
	 */
	private LocalDateTime lastModified;
	/**
	 * 文件大小
	 */
	private Long size;
	/**
	 * 头部信息
	 */
	private Map<String, List<String>> headers;
	/**
	 * 用户 元数据
	 */
	private Map<String, String> userMetadata;



}
