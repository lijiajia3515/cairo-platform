package io.github.lijiajia3515.cairo.auth.domain.api.client.menu;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取菜单树参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetMenuTreeArgs implements Serializable {

    /**
	 * 父级id
	 */
	private String parentId;

	/**
	 * 终端ID
	 */
	@NotBlank
	private String endpointId;

	/**
	 * subappId
	 */
	@NotBlank
	private String subappId;

	/**
	 * subappVersion
	 */
	@NotBlank
	private String subappVersion;


}
