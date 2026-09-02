package io.github.lijiajia3515.cairo.auth.modules.dict.sys;


import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountConverter;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictMongodb;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;

public class SysDictConverter {
	public static MetadataSysDict convertMetadataSysDict(SysDictMongodb mongo, Map<String, Account> accountMap) {
		return MetadataSysDict.builder()
			.dictId(mongo.getDictId())
			.dictName(mongo.getDictName())
			.dictType(mongo.getDictType())
			.icon(mongo.getIcon())
			.enabled(mongo.getEnabled())
			.isCreateItem(mongo.getIsCreateItem())
			.metadata(CairoAccountConverter.convertAccount(mongo.getMetadata(), accountMap))
			.build();
	}

	public static MetadataSysDictItem convertMetadataSysDictItem(SysDictItemMongodb mongo, Map<String, Account> accountMap) {
		return MetadataSysDictItem.builder()
			.parentItemId(mongo.getParentItemId())
			.itemId(mongo.getItemId())
			.itemName(mongo.getItemName())
			.remark(mongo.getRemark())
			.icon(mongo.getIcon())
			.editable(mongo.getEditable())
			.enabled(mongo.getEnabled())
			.depth(mongo.getDepth())
			.leftNo(mongo.getLeftNo())
			.rightNo(mongo.getRightNo())
			.metadata(CairoAccountConverter.convertAccount(mongo.getMetadata(), accountMap))
			.build();
	}


	public static SysDict convertSysDict(SysDictMongodb mongo) {
		return SysDict.builder().dictId(mongo.getDictId())
			.dictName(mongo.getDictName())
			.dictType(mongo.getDictType())
			.icon(mongo.getIcon())
			.enabled(mongo.getEnabled())
			.isCreateItem(mongo.getIsCreateItem())
			.build();
	}

	public static SysDict convertSysDict(SysDictMongodb mongo, List<SysDictItemMongodb> sortedItemMongodbList) {
		List<SysDictItem> itemList = Optional.ofNullable(sortedItemMongodbList).orElse(Collections.emptyList())
			.stream().map(SysDictConverter::convertSysDictItem).collect(Collectors.toList());
		List<SysDictItem> treeItemList = Tree2Converter.build(itemList, ROOT_ID);
		return SysDict.builder()
			.dictId(mongo.getDictId())
			.dictName(mongo.getDictName())
			.enabled(mongo.getEnabled())
			.icon(mongo.getIcon())
			.items(treeItemList)
			.build();
	}

	public static SysDictItem convertSysDictItem(SysDictItemMongodb mongo) {
		return SysDictItem.builder()
			.parentItemId(mongo.getParentItemId())
			.itemId(mongo.getItemId())
			.itemName(mongo.getItemName())
			.remark(mongo.getRemark())
			.icon(mongo.getIcon())
			.editable(mongo.getEditable())
			.enabled(mongo.getEnabled())
			.depth(mongo.getDepth())
			.leftNo(mongo.getLeftNo())
			.rightNo(mongo.getRightNo())
			.build();
	}
}
