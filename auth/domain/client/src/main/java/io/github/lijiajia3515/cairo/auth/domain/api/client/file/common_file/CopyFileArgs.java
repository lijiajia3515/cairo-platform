package io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CopyFileArgs implements Serializable {

	/**
	 * 源
	 */
	@NotNull
	private String source;

	/**
	 * 目标地址
	 */
	@NotNull
	private String target;
}
