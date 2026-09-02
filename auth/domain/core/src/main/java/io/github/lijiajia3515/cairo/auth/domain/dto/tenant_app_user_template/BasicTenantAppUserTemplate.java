package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;


/**
 * 企业用户模板
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BasicTenantAppUserTemplate implements Serializable {
	/**
	 * 用户ID
	 */
	private String tenantAppUserTemplateId;

	/**
	 * 昵称
	 */
	private String nickname;

}
