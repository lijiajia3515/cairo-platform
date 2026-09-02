package io.github.lijiajia3515.cairo.auth.domain.dto.file;

import io.github.lijiajia3515.cairo.core.tree.TreeConverter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FolderTools {

	public static List<Folder> convertFolderTree(List<String> folders, String root) {
		List<Folder> allFolderList = folders.stream().map(x -> {
			String parent = "";
			if (x.contains("/")) {
				parent = x.substring(0, x.lastIndexOf("/"));
			}
			return Folder.builder().parentId(parent).id(x).build();
		}).collect(Collectors.toList());

		return TreeConverter.build(allFolderList, root, Folder.COMPARATOR);
	}

	public static List<String> convertTempFolder(List<String> folders) {
		Set<String> folderSets = new HashSet<>();
		folders.forEach(folder -> {
			String[] split = folder.split("/");
			if (split.length > 0) {
				String tempFolder = split[0];
				folderSets.add(tempFolder);
				for (int i = 1; i < split.length; i++) {
					tempFolder += ("/" + split[i]);
					folderSets.add(tempFolder);
				}
			}
		});
		return folderSets.stream().sorted().collect(Collectors.toList());
	}

	public static List<Folder> convertTempFolderTree(List<String> folders, String root) {
		List<String> allFolders = convertTempFolder(folders);

		List<String> tempFolders = allFolders.stream().map(x -> root.isEmpty() ? x : root + "/" + x).toList();
		List<Folder> allFolderList = tempFolders.stream().map(x -> {
			String parent = root;
			if (x.contains("/")) {
				parent = x.substring(0, x.lastIndexOf("/"));
			}
			return Folder.builder().parentId(parent).id(x).build();
		}).collect(Collectors.toList());

		return TreeConverter.build(allFolderList, root, Folder.COMPARATOR);
	}
}
