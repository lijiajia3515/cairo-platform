package io.github.lijiajia3515.cairo.auth.domain.api.app_user.biz_log.app_biz_log;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetMyAppBizLogArgs extends AbstractPage<GetMyAppBizLogArgs> implements Serializable {
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
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 客户端ID
	 */
	private String clientId;

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
