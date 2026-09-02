package io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 删除公开存储文件参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteFileArgs {
	/**
	 * s3协议地址数组
	 */
	public List<String> s3Urls;

	/**
	 * http协议地址数组
	 */
	public List<String> httpUrls;
}
