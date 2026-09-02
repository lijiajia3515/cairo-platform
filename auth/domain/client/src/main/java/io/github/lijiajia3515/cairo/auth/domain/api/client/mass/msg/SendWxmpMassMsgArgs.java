package io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendWxmpMassMsgArgs {
	/**
	 * 标题
	 */
	@NotNull
	@NotBlank
	private String title;

	/**
	 * 封面url
	 */
	@NotNull
	@NotBlank
	private String coverUrl;

	/**
	 * 内容 富文本
	 */
	@NotNull
	@NotBlank
	private String content;

}
