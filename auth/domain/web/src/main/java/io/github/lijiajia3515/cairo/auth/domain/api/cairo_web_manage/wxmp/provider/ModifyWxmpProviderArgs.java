package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 修改微信公众号连接配置
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyWxmpProviderArgs implements Serializable {
	/**
	 * id
	 */
	@NotNull
	private String wxmpProviderId;

	/**
	 * 名称
	 */
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
