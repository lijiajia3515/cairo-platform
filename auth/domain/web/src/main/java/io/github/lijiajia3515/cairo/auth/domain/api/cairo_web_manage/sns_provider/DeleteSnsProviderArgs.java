package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除第三方认证提供方
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteSnsProviderArgs implements Serializable {

	/**
	 * 第三方认证提供方id
	 */
	@NotBlank
	private String snsProviderId;
}
