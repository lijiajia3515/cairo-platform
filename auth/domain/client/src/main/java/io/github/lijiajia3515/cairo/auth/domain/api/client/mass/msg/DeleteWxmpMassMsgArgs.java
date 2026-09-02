package io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteWxmpMassMsgArgs {
	/**
	 * 封面 MediaId
	 */
	@NotNull
	@NotBlank
	private String coverMediaId;

	/**
	 * 群发 MsgId
	 */
	@NotNull
	@NotBlank
	private String msgId;


	/**
	 * 内容 MediaId
	 */
	@NotNull
	@NotEmpty
	private List<String> contentMediaId;

}
