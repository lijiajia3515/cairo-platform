package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.tenant_subapp_biz_log;

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
 * 获取终端用户业务日志参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetTenantSubappBizLogArgs extends AbstractPage<GetTenantSubappBizLogArgs> implements Serializable {
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
	 * 产品ID
	 */
	private String subappId;

	/**
	 * 产品版本
	 */
	private String subappVersion;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 结果
	 */
	private Boolean success;


	/**
	 * 扩展插件
	 */
	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
