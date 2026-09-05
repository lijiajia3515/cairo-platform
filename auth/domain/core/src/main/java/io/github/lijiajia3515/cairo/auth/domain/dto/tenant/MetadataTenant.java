package io.github.lijiajia3515.cairo.auth.domain.dto.tenant;

import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.CairoAccountMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MetadataTenant implements Serializable {

	/**
	 * id
	 */
	private String tenantId;

	/**
	 * 企业名称
	 */
	private String tenantName;

	/**
	 * 别名
	 */
	private String aliasName;

	/**
	 * 拥有者ID
	 */
	private Account ownerAccount;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 是否开启
	 */
	private Boolean enabled;


	/**
	 * metadata
	 */
	private CairoAccountMetadata metadata;

}
