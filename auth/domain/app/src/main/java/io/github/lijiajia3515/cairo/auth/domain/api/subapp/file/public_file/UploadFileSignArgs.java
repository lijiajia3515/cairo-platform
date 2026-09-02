package io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.public_file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadFileSignArgs {
	/**
	 * 文件前缀
	 */
	private String keyPrefix;

	/**
	 * meta信息
	 */
	private Map<String, String> meta;

	/**
	 * 时长
	 */
	@Builder.Default
	private Duration ttl = Duration.ofMinutes(30);
}
