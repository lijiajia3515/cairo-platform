package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 创建第三方认证提供方参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateSnsProviderArgs implements Serializable {
	/**
	 * appId
	 */
	@NotBlank
	private String appId;

	/**
	 * ID
	 */
	@NotBlank
	private String snsProviderId;

	/**
	 * 名称
	 */
	@NotBlank
	private String snsProviderName;

	/**
	 * 类型
	 */
	@NotBlank
	private String snsProviderType;

	/**
	 * 厂商
	 */
	@NotBlank
	private String snsProviderPartner;

	/**
	 * clientId
	 */
	private String clientId;

	/**
	 * clientSecret
	 */
	private String clientSecret;

	/**
	 * 是否自动注册
	 */
	@Builder.Default
	private Boolean isAutoRegister=false;

}
