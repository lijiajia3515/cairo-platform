package io.github.lijiajia3515.cairo.core.tree;

import java.io.Serializable;
import java.util.List;

/**
 * 树形节点2
 *
 * @author lijiajia
 * @version 1.0
 */
public interface TreeNode2<ID, T> extends Serializable {
	/**
	 * ID
	 *
	 * @return ID
	 */
	ID id();

	/**
	 * 上级ID
	 *
	 * @return 上级ID
	 */
	ID parentId();

	/**
	 * 深度
	 * @return 深度
	 */
	int depth();

	/**
	 * 左值
	 * @return 右值
	 */
	int leftNo();

	/**
	 * 右值
	 * @return 右值
	 */
	int rightNo();

	/**
	 * 子集
	 *
	 * @return 子集
	 */
	List<T> subs();

	/**
	 * set subs
	 *
	 * @param subs 子集
	 */
	void subs(List<T> subs);
}
