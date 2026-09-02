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
public class ModifySnsProviderStatusArgs {
	/**
	 * 第三方认证提供方id
	 */
	@NotBlank
	private String snsProviderId;

	/**
	 * version
	 */
	@NotNull
	private long version;

	/**
	 * 状态
	 */
	@NotNull
	private Boolean enabled;

}
