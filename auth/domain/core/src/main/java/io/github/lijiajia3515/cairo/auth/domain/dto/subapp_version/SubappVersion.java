package io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 子应用版本 metadata
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SubappVersion implements Serializable {


	/**
	 * 子应用ID
	 */
	private String subappId;

	/**
	 * 子应用名称
	 */
	private String subappName;

	/**
	 * 子应用版本
	 */
	private String subappVersion;

	/**
	 * 子应用备注
	 */
	private String subappRemark;


	/**
	 * 是否开启
	 */
	private Boolean enabled;


}
