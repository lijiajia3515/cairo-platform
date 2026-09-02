package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
/**
 * 创建微信公众号连接配置
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateWxmpProviderArgs implements Serializable {

	/**
	 * 认证提供方id
	 */
	@NotNull
	private String wxmpProviderId;

	/**
	 * 认证提供方名称
	 */
	@NotNull
	private String wxmpProviderName;

	/**
	 * 设置微信公众号的appid.
	 */
	private String wxmpAppId;

	/**
	 * 设置微信公众号的app secret.
	 */
	private String wxmpSecret;

	/**
	 * 设置微信公众号的token.
	 */
	private String wxmpToken;

	/**
	 * 设置微信公众号的EncodingAESKey.
	 */
	private String wxmpAesKey;

}
