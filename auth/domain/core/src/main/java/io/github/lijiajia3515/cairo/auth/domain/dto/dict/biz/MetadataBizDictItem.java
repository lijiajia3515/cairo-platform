package io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.CairoTenantAppUserMetadata;
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
public class MetadataBizDictItem implements Serializable, TreeNode2<String, MetadataBizDictItem> {
	/**
	 * parentItemId
	 */
	private String parentItemId;
	/**
	 * id
	 */
	private String itemId;

	/**
	 * name
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
	 * metadata
	 */
	private CairoTenantAppUserMetadata metadata;

	/**
	 * 是否同步字典
	 */
	private Boolean isSync;


	/**
	 * 子项
	 */
	@Builder.Default
	private List<MetadataBizDictItem> subItems = new ArrayList<>(1);

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
	public List<MetadataBizDictItem> subs() {
		return subItems;
	}

	@Override
	public void subs(List<MetadataBizDictItem> subs) {
		this.subItems = subs;
	}
}
