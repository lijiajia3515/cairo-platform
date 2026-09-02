package io.github.lijiajia3515.cairo.auth.domain.dto.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 应用
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class App implements Serializable {

	/**
	 * appId
	 */
	private String appId;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 是否私有应用
	 */
	private Boolean privateApp;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 是否开启
	 */
	private Boolean enabled;

}
