package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 业务字典  添加字典项
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PutBizDictItemArgs implements Serializable {


	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

	/**
	 * 父级字典项ID
	 */
	private String parentItemId;

	/**
	 * 前字典项ID
	 */
	private String beforeItemId;
	/**
	 * 字典项ID
	 */
	private String itemId;

	/**
	 * 字典项名称
	 */
	private String itemName;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 编辑状态
	 */
	private Boolean editable;
}
