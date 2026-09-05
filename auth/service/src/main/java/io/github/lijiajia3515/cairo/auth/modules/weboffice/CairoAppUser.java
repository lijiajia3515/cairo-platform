package io.github.lijiajia3515.cairo.auth.modules.weboffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 应用级用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CairoAppUser implements Serializable {
	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 用户ID
	 */
	private String userId;
}
