package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 获取上传地址 返回值
 * {
 *   "code": 0,
 *   "data": {
 *     "method": "POST",
 *     "url": "http://foo.bar.com/files/27"
 *   },
 *   "msg": ""
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeFileUploadAddressResponse implements Serializable {

	/**
	 * 上传文档的 URL
	 */
	@JsonProperty("url")
	private String url;

	/**
	 * 上传文档的 HTTP Method，暂只支持 PUT，文件实体将在 Body 传递
	 */
	@JsonProperty("method")
	private String method;

	/**
	 * 上传文档时需要携带的额外请求头
	 */
	@JsonProperty("headers")
	private Map<String, String> headers;

	/**
	 * 上传文档时需要携带的额外参数，PUT 方式下在 Query 传递
	 */
	@JsonProperty("params")
	private Map<String, String> params;

	/**
	 * 上传文档后，请求完成上传接口需要原样带回的额外参数
	 */
	@JsonProperty("send_back_params")
	private Map<String,String> sendBackParams;

}
