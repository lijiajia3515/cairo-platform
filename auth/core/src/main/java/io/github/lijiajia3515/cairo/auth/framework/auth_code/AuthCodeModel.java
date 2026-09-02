package io.github.lijiajia3515.cairo.auth.framework.auth_code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 认证码模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthCodeModel implements Serializable {

	/**
	 * 认证code token
	 */
	private String authCode;

	/**
	 * ip 所属地
	 */
	private String ip;

	/**
	 * 是否活跃
	 */
	private boolean activated;

	/**
	 * 过期时间
	 */
	private LocalDateTime expireTime;
}
