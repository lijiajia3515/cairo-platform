package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 业务字典 修改字典项
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyBizDictItemInfoArgs implements Serializable {

	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

	/**
	 * 字典ID
	 */
	@NotNull
	private String itemId;

	/**
	 * 字典名称
	 */
	private String itemName;

	/**
	 * 字典名称
	 */
	private String remark;

	/**
	 * 图标值
	 */
	private String icon;

}
