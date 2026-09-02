package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 获取上传地址 请求参数
 *{
 *   "file_id": "27",
 *   "name": "样张.xlsx",
 *   "size": 11683,
 *   "digest": {
 *     "sha1": "asdjfiedjisdhihsidihishiahi"
 *   },
 *   "is_manual": true
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeFileUploadAddressRequest implements Serializable {
	/**
	 * 文档名称
	 */
	@JsonProperty("name")
	private String name;
	/**
	 * 文档大小，单位 byte
	 */
	@JsonProperty("size")
	private Integer size;

	/**
	 * 文档校验和，key 为算法，value 为结果值
	 */
	@JsonProperty("digest")
	private Map<String,String> digest;

	/**
	 * 是否手动保存，即用户手动 ctrl/cmd + s 或点击保存版本触发的保存，区别于定时触发的自动保存
	 */
	@JsonProperty("is_manual")
	private Boolean isManual;

	/**
	 * 文档内包含的附件的大小，单位 byte
	 */
	@JsonProperty("attachment_size")
	private Integer attachmentSize;

	/**
	 * 文档的 MIME 类型
	 */
	@JsonProperty("content_type")
	private String contentType;
}
