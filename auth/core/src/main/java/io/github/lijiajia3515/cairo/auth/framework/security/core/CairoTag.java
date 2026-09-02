package io.github.lijiajia3515.cairo.auth.framework.security.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 角色 V1
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CairoTag implements Serializable {

	/**
	 * 值
	 */
	private String tagId;

	/**
	 * 名称
	 */
	private String tagName;

}
