package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业角色模板
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MetadataTenantAppRoleTemplate implements Serializable {

	/**
	 * id
	 */
	private String tenantAppRoleTemplateId;

	/**
	 * 名称
	 */
	private String tenantAppRoleTemplateName;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 用户数量
	 */
	private Integer userNum;

	/**
	 * 元信息
	 */
	private CairoAppUserMetadata metadata;

}
