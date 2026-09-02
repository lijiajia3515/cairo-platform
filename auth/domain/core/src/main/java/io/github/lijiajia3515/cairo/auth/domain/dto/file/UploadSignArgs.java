package io.github.lijiajia3515.cairo.auth.domain.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 上传文件签名(form-data)
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UploadSignArgs {
	/**
	 * 端点
	 */
	private String endpoint;

	/**
	 * 存储桶
	 */
	private String bucket;

	/**
	 * keyPrefix
	 */
	private String keyPrefix;

	/**
	 * 过期时间
	 */
	private LocalDateTime expiresTime;

	/**
	 * 参数
	 */
	private Map<String, String> signPostFormData;
}
