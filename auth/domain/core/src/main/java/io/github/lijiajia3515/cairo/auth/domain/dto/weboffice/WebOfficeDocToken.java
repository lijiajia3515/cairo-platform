package io.github.lijiajia3515.cairo.auth.domain.dto.weboffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Doc Info
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeDocToken implements Serializable {
	/**
	 * appId
	 */
	private String appId;
	/**
	 * 文件id
	 */
	private String fileId;
	/**
	 * 文件类型
	 */
	private String type;
	/**
	 * 交互token
	 */
	private String token;

	/**
	 * 是否允许写入
	 */
	private Boolean write;
}
