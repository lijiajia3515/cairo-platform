package io.github.lijiajia3515.cairo.auth.domain.api.client.sns_provider;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 获取第三方认证提供方集合参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetSnsProviderArgs extends AbstractPage<GetSnsProviderArgs> implements Serializable {

	/**
	 * 应用id
	 */
	private String appId;

	/**
	 * 第三方账号认证ID
	 */
	private List<String> snsProviderIds;

	/**
	 * 第三方认证类型
	 */
	private List<String> snsTypes;

	/**
	 * 第三方认证厂商
	 */
	private List<String> snsPartners;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 关键字
	 */
	private String keyword;

}
