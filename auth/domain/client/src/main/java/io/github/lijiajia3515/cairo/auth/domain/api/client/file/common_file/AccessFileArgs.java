package io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AccessFileArgs implements Serializable {

	/**
	 * 文件路径
	 */
	@NotNull
	@NotEmpty
	private List<String> s3Urls;

	/**
	 * 访问时长
	 */
	private Duration ttl;

	/**
	 * 是否开启version访问
	 */
	@Builder.Default
	private boolean enableVersion = false;
}
