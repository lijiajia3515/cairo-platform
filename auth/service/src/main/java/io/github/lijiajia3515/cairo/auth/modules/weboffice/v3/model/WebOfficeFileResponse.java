package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回值示例
 * {
 *   "code": 0,
 *   "data": {
 *     "create_time": 1670218748,
 *     "creator_id": "404",
 *     "id": "9",
 *     "modifier_id": "404",
 *     "modify_time": 1670328304,
 *     "name": "统计月报.xlsx",
 *     "size": 18961,
 *     "version": 180
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeFileResponse {
	/**
	 * 文档 ID
	 */
	@JsonProperty("id")
	private String id;

	/**
	 * 文档名称，最大长度240，不能包含下列特殊字符：{@code \|":*?<>}
	 */
	@JsonProperty("name")
	private String name;

	/**
	 * 文档版本号，从 1 开始，每次保存后递增
	 */
	@JsonProperty("version")
	private Integer version;

	/**
	 * 文档大小，单位 byte
	 */
	@JsonProperty("size")
	private Integer size;

	/**
	 * 文档创建时间戳，单位纪元秒
	 */
	@JsonProperty("create_time")
	private Integer createTime;

	/**
	 * 文档最后修改时间戳，单位纪元秒
	 */
	@JsonProperty("modify_time")
	private Integer modifyTime;

	/**
	 * 文档创建者 Id
	 */
	@JsonProperty("creator_id")
	private String creatorId;

	/**
	 * 文档最后修改者 Id
	 */
	@JsonProperty("modifier_id")
	private String modifierId;
}
