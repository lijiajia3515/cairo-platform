package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 修改微信公众号连接配置状态
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyWxmpProviderStatusArgs implements Serializable {
	/**
	 * id
	 */
	@NotNull
	private String wxmpProviderId;


	/**
	 * 启用/禁用.
	 */
	private Boolean enabled;
}
