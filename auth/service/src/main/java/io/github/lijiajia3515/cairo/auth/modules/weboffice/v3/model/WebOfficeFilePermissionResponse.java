package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文档用户权限
 * {
 *   "code": 0,
 *   "data": {
 *     "comment": 1,
 *     "copy": 1,
 *     "download": 1,
 *     "history": 1,
 *     "print": 1,
 *     "read": 1,
 *     "rename": 1,
 *     "saveas": 1,
 *     "update": 1,
 *     "user_id": "404"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeFilePermissionResponse implements Serializable {
	/**
	 * 当前用户 Id，如匿名情况下可为空
	 */
	@JsonProperty("user_id")
	private String userId;

	/**
	 * 是否具有预览权限，0-无 1-有
	 */
	@JsonProperty("read")
	@Builder.Default
	private int read = 1;

	/**
	 * 是否具有编辑权限，0-无 1-有
	 */
	@JsonProperty("update")
	@Builder.Default
	private int update = 0;

	/**
	 * 是否具有下载文档权限，0-无 1-有
	 */
	@JsonProperty("download")
	@Builder.Default
	private int download = 0;

	/**
	 * 是否具有重命名文档权限，0-无 1-有
	 */
	@JsonProperty("rename")
	@Builder.Default
	private int rename = 0;
	/**
	 * 是否具有查看文档历史记录权限，0-无 1-有
	 */
	@JsonProperty("history")
	@Builder.Default
	private int history = 0;

	/**
	 * 	是否具有拷贝文档内容权限，0-无 1-有
	 */
	@JsonProperty("copy")
	@Builder.Default
	private int copy = 0;

	/**
	 * 是否具有打印文档权限，0-无 1-有
	 */
	@JsonProperty("print")
	@Builder.Default
	private int print = 0;

	/**
	 * 是否具有另存当前文档权限，0-无 1-有
	 */
	@JsonProperty("saveas")
	@Builder.Default
	private int saveas = 0;

	/**
	 * 是否具有评论文档权限，0-无 1-有
	 */
	@JsonProperty("comment")
	@Builder.Default
	private int comment = 0;

}
