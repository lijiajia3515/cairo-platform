package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.CairoTenantAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 角色
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MetadataTenantAppRole implements Serializable {
	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * id
	 */
	private String roleId;

	/**
	 * 名称
	 */
	private String roleName;

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
	private CairoTenantAppUserMetadata metadata;

}
