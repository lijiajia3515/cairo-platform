package io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.wxmp.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 获取我的企业用户公众号绑定信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyWxmpTenantAppUser {

	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 公众号ID
	 */
	private String wxProviderId;

	/**
	 * 公众号名称
	 */
	private String wxProviderName;

	/**
	 * 公众号认证accessKey
	 */
	private String accessKey;


	/**
	 * 第三方认证唯一标识-openId
	 */
	private String openId;

	/**
	 * 绑定时间
	 */
	private LocalDateTime bindTime;


	/**
	 * 是否绑定
	 */
	private Boolean isBind;

}
