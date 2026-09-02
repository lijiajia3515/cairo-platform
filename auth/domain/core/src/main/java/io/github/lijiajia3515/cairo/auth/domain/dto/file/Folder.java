package io.github.lijiajia3515.cairo.auth.domain.dto.file;

import io.github.lijiajia3515.cairo.core.tree.TreeNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder implements TreeNode<String, Folder> {
	public static final Comparator<Folder> COMPARATOR = Comparator.comparing(Folder::id);

	private String parentId;
	private String id;

	public Map<String,String> userMetadata;

	@Builder.Default
	private List<Folder> subs = new ArrayList<>();

	@Override
	public String id() {
		return id;
	}

	@Override
	public String parent() {
		return parentId;
	}

	@Override
	public List<Folder> subs() {
		return subs;
	}

	@Override
	public void subs(List<Folder> subs) {
		this.subs = subs;
	}

}
