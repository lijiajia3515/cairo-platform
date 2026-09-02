package io.github.lijiajia3515.cairo.auth.modules.ip2region;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionModel {

	/**
	 * 国家
	 */
	private String country;

	/**
	 * id
	 */
	private String id;

	/**
	 * 省
	 */
	private String province;

	/**
	 * 市
	 */
	private String city;

	/**
	 * 运营商
	 */
	private String isp;
}
