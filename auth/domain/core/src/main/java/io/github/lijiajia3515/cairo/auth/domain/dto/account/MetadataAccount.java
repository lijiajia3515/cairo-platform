package io.github.lijiajia3515.cairo.auth.domain.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账号
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataAccount implements Serializable {
	/**
	 * 账号ID
	 */
	private String accountId;


	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 昵称
	 */
	private String nickname;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 邮箱
	 */
	private String email;
	/**
	 * 手机号
	 */
	private String phoneNumber;
	/**
	 * 启用状态
	 */
	private Boolean enabled;
	/**
	 * 锁定状态
	 */
	private Boolean locked;

	/**
	 * 加入时间
	 */
	private LocalDateTime joinTime;

	/**
	 * 最后登录时间
	 */
	private LocalDateTime loginTime;

	/**
	 * 注销状态
	 */
	private String logoffStatus;

	/**
	 * 注销时间
	 */
	private LocalDateTime logoffPendingTime;
	/**
	 * 注销成功时间
	 */
	private LocalDateTime logoffSuccessTime;


	/**
	 * metadata
	 */
	private CairoAccountMetadata metadata;
}
