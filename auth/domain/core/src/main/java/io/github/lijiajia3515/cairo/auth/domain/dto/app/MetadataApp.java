package io.github.lijiajia3515.cairo.auth.domain.dto.app;

import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 应用
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MetadataApp implements Serializable {

	/**
	 * appId
	 */
	private String appId;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 范围
	 */
	private List<String> scopes;

	/**
	 * 是否私有应用
	 */
	private Boolean privateApp;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 是否开启
	 */
	private Boolean enabled;

	/**
	 * 管理员账号
	 */
	private List<Account> adminAccounts;

	/**
	 * 自动注册
	 */
	private Boolean autoRegister;



	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;

}
