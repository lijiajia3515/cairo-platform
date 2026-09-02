package io.github.lijiajia3515.cairo.auth.domain.dto.app_role;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 应用角色
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MetadataAppRole implements Serializable {

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
	private CairoAppUserMetadata metadata;

}
