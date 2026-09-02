package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_partner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifySnsProviderInfoArgs {
	/**
	 * ID
	 */
	@NotBlank
	private String snsProviderId;

	/**
	 * 名称
	 */
	private String snsProviderName;

	/**
	 * 类型
	 */
	private String snsProviderType;

	/**
	 * 厂商
	 */
	private String snsProviderPartner;

	/**
	 * AccessKey
	 */
	private String accessKey;

	/**
	 * SecretKey
	 */
	private String secretKey;


	/**
	 * 是否自动注册
	 */
	private Boolean isAutoRegister;

	/**
	 * logo图标
	 */
	private String icon;
	/**
	 * version
	 */
	@NotNull
	private long version;


}
