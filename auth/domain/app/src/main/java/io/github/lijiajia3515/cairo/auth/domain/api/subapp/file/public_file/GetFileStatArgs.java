package io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.public_file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 获取文件状态参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetFileStatArgs implements Serializable {

	/**
	 * s3协议地址
	 */
	@NotNull
	@NotEmpty
	private List<String> s3Urls;

	@Builder.Default
	private boolean enableVersion = false;
}
