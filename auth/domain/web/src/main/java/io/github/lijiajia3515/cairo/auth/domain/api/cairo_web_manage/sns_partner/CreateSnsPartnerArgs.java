package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_partner;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 创建第三方厂商
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateSnsPartnerArgs implements Serializable {

	/**
	 * ID
	 */
	@NotBlank
	private String snsPartnerId;

	/**
	 * 名称
	 */
	@NotBlank
	private String snsPartnerName;


	/**
	 * logo图标
	 */
	private String icon;


}
