package io.github.lijiajia3515.cairo.auth.domain.message.dict.sys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 复制系统字典消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopySysDictMessage {

	/**
	 * 当前应用id
	 */
	private String currentAppId;

	/**
	 * 复制应用id
	 */
	private String copyAppId;


	/**
	 * 账号id
	 */
	private String eventAccountId;

	/**
	 * 字典ID
	 */
	private String dictId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
