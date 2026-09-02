package io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendWxmpMsgByArgs {


	/**
	 * 接受应用用户ids
	 */
	@NotNull
	@Size(min = 1)
	private Set<String> toAppUserIds;

	/**
	 * 业务id
	 */
	@NotNull
	@NotBlank
	private String bizId;

	/**
	 * 业务参数
	 */
	@NotNull
	private Map<String, MessageContent> params;

	/**
	 * 跳转链接
	 */
	private String jumpUrl;


	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class MessageContent {

		/**
		 * 消息内容
		 */
		@NotNull
		@NotBlank
		private String content;


		/**
		 * 颜色
		 */
		private String color;

	}
}
