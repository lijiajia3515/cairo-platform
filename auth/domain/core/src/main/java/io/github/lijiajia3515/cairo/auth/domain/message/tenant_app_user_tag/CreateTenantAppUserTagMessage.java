package io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user_tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateTenantAppUserTagMessage implements Serializable {
	/**
	 * 企业id
	 */
	private String tenantId;

	/**
	 * 应用id
	 */
	private String appId;

	/**
	 * 标签ID
	 */
	private String tagId;

	/**
	 * 标签名称
	 */
	private String tagName;

	/**
	 * 事件用户ID
	 */
	private String eventUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
