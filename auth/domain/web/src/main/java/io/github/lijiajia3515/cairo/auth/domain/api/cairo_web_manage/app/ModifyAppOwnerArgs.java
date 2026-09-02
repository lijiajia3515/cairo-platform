package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改应用参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAppOwnerArgs implements Serializable {

	/**
	 * appId
	 */
	@NotNull
	@NotBlank
	private String appId;



	/**
	 * 拥有着账号ID
	 */
	private String ownerAccountId;
}
