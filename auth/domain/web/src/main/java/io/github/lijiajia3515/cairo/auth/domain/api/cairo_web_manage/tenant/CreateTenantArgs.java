package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 创建企业参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateTenantArgs implements Serializable {

	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 企业名称
	 */
	@NotNull
	@NotBlank
	private String tenantName;

	/**
	 * 别名
	 */
	private String aliasName;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 拥有者（账号ID）
	 */
	@NotBlank
	private String ownerAccountId;

	/**
	 * 启用状态
	 */
	@Builder.Default
	private boolean enabled = true;
}
