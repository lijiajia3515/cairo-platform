package io.github.lijiajia3515.cairo.auth.domain.api.client.menu;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 获取菜单集合参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetMenuListArgs implements Serializable {

    /**
	 * 父级ID
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
	/**
	 * menuId
	 */
	private Set<String> menuIds;


	/**
	 * 不包含左右值
	 */
	private List<MenuNo> menuNos;

	/**
	 * 大于左值
	 */
	private Integer leftNo;

	/**
	 * 小于右值
	 */
	private Integer rightNo;

	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class MenuNo{

		/**
		 * 左值
		 */
		private Integer leftNo;

		/**
		 * 右值
		 */
		private Integer rightNo;
	}

}
