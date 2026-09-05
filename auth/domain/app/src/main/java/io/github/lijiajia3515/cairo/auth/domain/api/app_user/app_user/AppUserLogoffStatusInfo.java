package io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 应用级用户
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AppUserLogoffStatusInfo implements Serializable {

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
}
