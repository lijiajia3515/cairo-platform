package io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
	 * 企业ID
	 */
	@NotNull
	private String tenantId;

	/**
	 * s3协议地址
	 */
	private List<String> s3Urls;


	/**
	 * http协议地址
	 */
	private List<String> httpUrls;

	@Builder.Default
	private boolean enableVersion = false;
}
