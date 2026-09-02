package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 上传完成后，回调通知上传结果
 * {
 *   "request": {
 *     "file_id": "27",
 *     "name": "样张.xlsx",
 *     "size": 11683,
 *     "digest": { "sha1": "foo" },
 *     "is_manual": true
 *   },
 *   "response": {
 *     "status_code": 200,
 *     "headers": { "etag": "bar" }
 *   },
 *   "send_back_params": {
 *     "foo": "bar"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeFileUploadCompleteRequest implements Serializable {
	/**
	 * 获取上传地址时相同的请求
	 */
	@JsonProperty("request")
	private Request request;

	/**
	 * 上传文档完成后，存储服务返回的 HTTP Response
	 */
	@JsonProperty("response")
	private Response response;
	/**
	 * 获取上传地址时，要求原样带回的额外参数
	 */
	@JsonProperty("send_back_params")
	private Map<String, String> sendBackParams;

	/**
	 * 获取上传地址时相同的请求
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Request {

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
		private Map<String, String> digest;

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

	/**
	 * 上传文档完成后，存储服务返回的 HTTP Response
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		/**
		 * 上传文档时，存储服务返回的 HTTP Response Status
		 */
		@JsonProperty("status_code")
		private Integer statusCode;

		/**
		 * 上传文档时，存储服务返回的 HTTP Response Header
		 */
		@JsonProperty("headers")
		private Map<String, String> headers;

		/**
		 * 上传文档时，存储服务返回的 HTTP Response Body 的 base64 编码
		 */

		@JsonProperty("body")
		private Byte[] body;

	}
}
