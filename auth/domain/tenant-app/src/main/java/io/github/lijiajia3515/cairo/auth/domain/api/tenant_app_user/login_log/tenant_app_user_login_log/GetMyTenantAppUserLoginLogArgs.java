package io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.login_log.tenant_app_user_login_log;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetMyTenantAppUserLoginLogArgs extends AbstractPage<GetMyTenantAppUserLoginLogArgs> implements Serializable {

	/**
	 * 搜索参数
	 */
	private String keyword;

	/**
	 * 开启时间
	 */
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	private LocalDateTime endTime;

	/**
	 * 终端标识
	 */
	private String endpointId;

	/**
	 * 客户端ID
	 */
	private String clientId;

	/**
	 * 登录方式
	 */
	private String loginType;

	/**
	 * 是否成功
	 */
	private Boolean success;


	/**
	 * 扩展插件
	 */
	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
