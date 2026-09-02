package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 源数据 对象
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CairoTenantAppUserMetadata implements Serializable {

	/**
	 * 创建用户
	 */
	private TenantAppUser createUser;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 创建用户
	 */
	private TenantAppUser updateUser;

	/**
	 * 最后修改时间
	 */
	private LocalDateTime updateTime;
}
