package io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys;

import io.github.lijiajia3515.cairo.core.tree.TreeNode2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SysDictItem implements Serializable, TreeNode2<String, SysDictItem> {
	/**
	 * 父级字典项ID
	 */
	private String parentItemId;

	/**
	 * 字典项ID
	 */
	private String itemId;

	/**
	 * 字典项名称
	 */
	private String itemName;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 编辑状态
	 */
	private Boolean editable;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 深度
	 */
	private Integer depth;

	/**
	 * 左值
	 */
	private Integer leftNo;

	/**
	 * 右值
	 */
	private Integer rightNo;

	/**
	 * 子项
	 */
	@Builder.Default
	private List<SysDictItem> subItems = new ArrayList<>(1);

	@Override
	public String id() {
		return itemId;
	}

	@Override
	public String parentId() {
		return parentItemId;
	}

	@Override
	public int depth() {
		return depth;
	}

	@Override
	public int leftNo() {
		return leftNo;
	}

	@Override
	public int rightNo() {
		return rightNo;
	}


	@Override
	public List<SysDictItem> subs() {
		return subItems;
	}

	@Override
	public void subs(List<SysDictItem> subs) {
		this.subItems = subs;
	}
}
