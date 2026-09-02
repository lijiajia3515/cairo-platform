package io.github.lijiajia3515.cairo.auth.domain.api.endpoint.wxmp;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 绑定三方应用用户
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BindAppUserWxmpArgs implements Serializable {

	/**
	 * 微信ID
	 */
	@NotNull
	private String wxProviderId;

	/**
	 * openId
	 */
	@NotNull
	private String openId;
}
