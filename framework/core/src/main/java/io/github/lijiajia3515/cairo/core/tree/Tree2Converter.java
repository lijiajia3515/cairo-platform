package io.github.lijiajia3515.cairo.core.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree2 节点构建Util
 *
 * @author lijiajia
 * @version 1.0
 */
public class Tree2Converter {
	/**
	 * @param sortedNodes 节点 集合
	 * @param rootId      根节点
	 * @param <ID>        节点标识
	 * @param <T>         节点数据
	 * @param <Node>      节点 类型
	 * @return 集合 属性节点
	 */
	public static <ID, T, Node extends TreeNode2<ID, Node>> List<Node> build(List<Node> sortedNodes, ID rootId) {
		List<Node> treeNodes = new ArrayList<>();
		Node parentNode = null;
		for (Node node : sortedNodes) {
			// 当前节点匹配上次查找的节点是否未父子级关系
			if (matchParentNode(node, parentNode)) {
				// 从缓存中add
				parentNode.subs().add(node);
			} else {
				//
				parentNode = getParentNode(node, treeNodes);
				if (parentNode == null) {
					// 从根节点添加
					treeNodes.add(node);
				} else {
					// 从父节点添加
					parentNode.subs().add(node);
				}
			}
		}
		if (!treeNodes.isEmpty()) {
			for (Node treeNode : treeNodes) {
				if (treeNode.id().equals(rootId)) {
					return treeNode.subs();
				}
			}
		}
		return treeNodes;
	}

	/**
	 * 在树结构中查准当前节点的父级节点
	 *
	 * @param treeNodes   树结构
	 * @param currentNode 当前节点
	 * @param <ID>        节点ID类型
	 * @param <Node>      节点类型
	 * @return 当前节点的父节点
	 */
	private static <ID, Node extends TreeNode2<ID, Node>> Node getParentNode(Node currentNode, List<Node> treeNodes) {
		for (Node node : treeNodes) {
			if (currentNode.leftNo() > node.leftNo() && currentNode.rightNo() < node.rightNo()) {
				if (currentNode.depth() > node.depth() + 1) {
					return getParentNode(currentNode, node.subs());
				}
				if (currentNode.parentId().equals(node.id())) {
					return node;
				}
			}
		}
		return null;
	}

	/**
	 * 判断currentDate是parentNode的子集
	 *
	 * @param currentNode 当前节点
	 * @param parentNode  父级节点
	 * @param <ID>        节点ID类型
	 * @param <Node>      节点类型
	 * @return 是否匹配
	 */
	private static <ID, Node extends TreeNode2<ID, Node>> boolean matchParentNode(Node currentNode, Node parentNode) {
		if (parentNode == null || currentNode == null) {
			return false;
		}
		return currentNode.leftNo() > parentNode.leftNo() && currentNode.rightNo() < parentNode.rightNo() && currentNode.parentId().equals(parentNode.id());
	}
}
