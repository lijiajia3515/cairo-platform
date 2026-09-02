package io.github.lijiajia3515.cairo.auth.domain.dto.area;

import io.github.lijiajia3515.cairo.core.tree.TreeNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 区域树
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AreaTree implements TreeNode<String, AreaTree>, Serializable {
	public static final Comparator<AreaTree> COMPARATOR = Comparator.comparing(AreaTree::getSort);
	/**
	 * 上级区域ID
	 */
	private String parentAreaId;

	/**
	 * 区域ID
	 */
	private String areaId;

	/**
	 * 区域名称
	 */
	private String areaName;

	/**
	 * 拼音前缀
	 */
	private String pinYinPrefix;

	/**
	 * 热门
	 */
	private boolean hot;

	/**
	 * 状态
	 */
	private boolean enabled;

	/**
	 * 层级
	 */
	private int depth;

	/**
	 * 排序值
	 */
	private int sort;

	@Builder.Default
	private List<AreaTree> subs = new ArrayList<>();

	@Override
	public String id() {
		return areaId;
	}

	@Override
	public String parent() {
		return parentAreaId;
	}

	@Override
	public List<AreaTree> subs() {
		return subs;
	}

	@Override
	public void subs(List<AreaTree> subs) {
		this.subs = subs;
	}
}
