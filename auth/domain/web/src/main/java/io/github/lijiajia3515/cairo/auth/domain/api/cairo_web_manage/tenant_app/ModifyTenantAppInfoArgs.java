package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_app;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 企业修改信息
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyTenantAppInfoArgs implements Serializable {

	/**
	 * tenantId
	 */
	@NotNull
	private String tenantId;

	/**
	 * appId
	 */
	@NotNull
	private String appId;


	/**
	 * 是否开启自动注册
	 */
	private Boolean autoRegister;

	/**
	 * 管理员账号
	 */
	private List<String> adminAccountIds;
}
