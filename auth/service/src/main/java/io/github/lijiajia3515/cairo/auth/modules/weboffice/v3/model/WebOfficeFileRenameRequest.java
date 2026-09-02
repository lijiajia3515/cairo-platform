package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取上传地址 请求参数
 * { "name": "新文件名.xlxs" }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeFileRenameRequest implements Serializable {
	/**
     * 新文档名称
	 */
	@JsonProperty("name")
	private String name;

}
