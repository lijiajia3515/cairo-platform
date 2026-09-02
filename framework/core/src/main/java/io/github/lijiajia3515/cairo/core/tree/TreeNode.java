package io.github.lijiajia3515.cairo.core.tree;

import java.io.Serializable;
import java.util.List;

/**
 * 树形节点
 *
 * @author lijiajia
 * @version 1.0
 */
public interface TreeNode<ID, T> extends Serializable {
	/**
	 * id
	 *
	 * @return id
	 */
	ID id();

	/**
	 * 上级
	 *
	 * @return 上级
	 */
	ID parent();

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
