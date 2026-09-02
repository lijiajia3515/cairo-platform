package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 准备上传阶段
 * {
 *   "code": 0,
 *   "data": {
 *     "digest_types": ["sha1"]
 *   },
 *   "msg": ""
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeFileUploadPrepareResponse {

	/**
	 * 文档校验和算法 "md5" "sha1" "sha256"
	 */
	@JsonProperty("digest_types")
	private Set<String> digestTypes;
}
