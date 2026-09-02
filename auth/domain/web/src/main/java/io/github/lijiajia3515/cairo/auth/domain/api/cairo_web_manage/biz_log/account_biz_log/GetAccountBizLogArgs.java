package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.account_biz_log;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取账号级业务日志参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetAccountBizLogArgs extends AbstractPage<GetAccountBizLogArgs> implements Serializable {
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
     * 账号ID
	 */
	private String accountId;

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
