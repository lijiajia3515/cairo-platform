package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改client secret 参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyClientSecretArgs implements Serializable {

	/**
	 * client id
	 */
	@NotNull
	private String id;


	/**
	 * 客户端标识
	 */
	@NotNull
	private String clientId;

	/**
	 * clientSecret
	 */
	private String clientSecret;
}
