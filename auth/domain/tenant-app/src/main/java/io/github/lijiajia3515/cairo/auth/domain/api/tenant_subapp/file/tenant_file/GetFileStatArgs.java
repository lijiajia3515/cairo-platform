package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 获取公开文件状态参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetFileStatArgs implements Serializable {

	/**
	 * s3协议地址
	 */
	private List<String> s3Urls;


	/**
	 * http协议地址
	 */
	private List<String> httpUrls;

	/**
	 * 是否启用版本控制
	 */
	private boolean enableVersion;
}
