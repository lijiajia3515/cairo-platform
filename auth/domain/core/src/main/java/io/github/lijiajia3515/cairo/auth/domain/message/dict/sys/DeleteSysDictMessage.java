package io.github.lijiajia3515.cairo.auth.domain.message.dict.sys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 删除系统字典消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteSysDictMessage {

	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 应用id
	 */
	private String appId;

	/**
	 * 字典ID
	 */
	private String dictId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
