package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 创建企业用用参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateTenantAppArgs implements Serializable {

	/**
	 * 企业ID
	 */
	@NotNull
	private String tenantId;

	/**
	 * 管理员账号ID
	 */
	private List<String> adminAccountIds;

	/**
	 * 开通的终端Ids
	 */
	private List<String> endpointIds;

	/**
	 * 开通的企业子应用Ids
	 */
	private List<String> subappIds;

	/**
	 * 自动注册
	 */
	private boolean autoRegister;


	/**
	 * 启用状态
	 */
	@Builder.Default
	private Boolean enabled = true;
}
