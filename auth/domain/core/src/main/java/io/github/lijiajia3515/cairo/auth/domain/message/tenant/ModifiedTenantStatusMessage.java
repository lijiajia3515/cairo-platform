package io.github.lijiajia3515.cairo.auth.domain.message.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建企业消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedTenantStatusMessage {
    /**
     * tenantId
     */
    private String tenantId;

    /**
     * 状态
     */
    private Boolean enabled;

	/**
	 * 账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
