package io.github.lijiajia3515.cairo.core.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class Test {
	public static void main(String[] args) {
		List<A> a1List = new ArrayList<>();
		List<A> a2List = new ArrayList<>();
		List<A> a3List = new ArrayList<>();
		List<A> aList = new ArrayList<>();
		for (int j = 1; j <= 5; j++) {
			A l1 = A.builder()
				.id(String.format("%s", j))
				.parentId("0")
				.name(String.format("%s", j))
				.depth(1)
				.leftNo((j - 1) * 60 + ((j - 1) * 2) + 1)
				.rightNo((j) * 60 + (j * 2))
				.build();
			a1List.add(l1);
			for (int k = 1; k <= 5; k++) {
				A l2 = A.builder()
					.id(String.format("%s-%s", j, k))
					.parentId(String.format("%s", j))
					.name(String.format("%s-%s", j, k))
					.depth(2)
					.leftNo(((j - 1) * 60 + ((j - 1) * 2) + 1 + ((k - 1) * 12 + 1)))
					.rightNo((j - 1) * 60 + ((j - 1) * 2) + 1 + ((k) * 12))
					.build();
				a2List.add(l2);
				for (int l = 1; l <= 5; l++) {
					A l3 = A.builder()
						.id(String.format("%s-%s-%s", j, k, l))
						.parentId(String.format("%s-%s", j, k))
						.name(String.format("%s-%s-%s", j, k, l))
						.depth(3)
						.leftNo((j - 1) * 60 + ((j - 1) * 2) + 1 + ((k - 1) * 12 + 1) + ((l - 1) * 2 + 1))
						.rightNo((j - 1) * 60 + ((j - 1) * 2) + 1 + ((k - 1) * 12 + 1) + (l * 2))
						.build();
					a3List.add(l3);
				}
			}
		}
		aList.addAll(a1List);
		aList.addAll(a2List);
		aList.addAll(a3List);
		for (A a : aList) {
			System.out.printf("Id: %s, ParentId: %s Depth: %s LeftNo: %s RightNo: %s%n", a.id, a.parentId, a.getDepth(), a.leftNo, a.rightNo);
		}
		List<A> build = Tree2Converter.build(aList, "0");
		for (A a : build) {
			System.out.printf("L1: Id: %s, ParentId: %s Depth: %s LeftNo: %s RightNo: %s%n", a.id, a.parentId, a.getDepth(), a.leftNo, a.rightNo);
			for (A b : a.subs) {
				System.out.printf("\t L2: Id: %s, ParentId: %s Depth: %s LeftNo: %s RightNo: %s%n", b.id, b.parentId, b.getDepth(), b.leftNo, b.rightNo);
				for (A c : b.subs) {
					System.out.printf("\t\t L3: Id: %s, ParentId: %s Depth: %s LeftNo: %s RightNo: %s%n", c.id, c.parentId, c.getDepth(), c.leftNo, c.rightNo);

				}
			}
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class A implements TreeNode2<String, A> {
		private String id;
		private String name;
		private String parentId;
		private int depth;
		private int leftNo;
		private int rightNo;

		@Builder.Default
		private List<A> subs = new ArrayList<>();

		@Override
		public String id() {
			return id;
		}

		@Override
		public String parentId() {
			return parentId;
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
		public List<A> subs() {
			return subs;
		}

		@Override
		public void subs(List<A> subs) {
			this.subs = subs;
		}
	}
}
