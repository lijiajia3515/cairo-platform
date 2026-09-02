package io.github.lijiajia3515.cairo.auth.domain.message.dict.sys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 同步系统字典消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncSysDictMessage {

	/**
	 * 账号ID
	 */
	private String eventAccountId;

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
