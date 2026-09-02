package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_partner;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除第三方厂商
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteSnsPartnerArgs implements Serializable {

	/**
	 * 第三方厂商id
	 */
	@NotBlank
	private String snsPartnerId;
}
