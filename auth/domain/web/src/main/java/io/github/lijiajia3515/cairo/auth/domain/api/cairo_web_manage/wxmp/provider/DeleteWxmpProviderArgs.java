package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 删除微信公众号连接配置
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteWxmpProviderArgs implements Serializable {

	/**
	 * ID
	 */
	@NotNull
	private String wxmpProviderId;

}
