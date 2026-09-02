package io.github.lijiajia3515.cairo.auth.modules.dict.biz;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserConverter;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.MetadataBizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.MetadataBizDictItem;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictMongodb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;

public class BizDictConverter {
	public static MetadataBizDict convertMetadataBizDict(BizDictMongodb mongo, Map<String, TenantAppUser> metadataUserMap) {
		return MetadataBizDict.builder()
			.dictId(mongo.getDictId())
			.dictName(mongo.getDictName())
			.icon(mongo.getIcon())
			.enabled(mongo.getEnabled())
			.isCreateItem(mongo.getIsCreateItem())
			.metadata(CairoTenantAppUserConverter.convertTenantAppUser(mongo.getMetadata(), metadataUserMap))
			.build();
	}

	public static BizDict convertBizDict(BizDictMongodb mongo) {
		return BizDict.builder()
			.dictId(mongo.getDictId())
			.dictName(mongo.getDictName())
			.icon(mongo.getIcon())
			.enabled(mongo.getEnabled())
			.build();
	}

	public static MetadataBizDictItem convertMetadataBizDictItem(BizDictItemMongodb mongo, Map<String, TenantAppUser> metadataUserMap) {
		return MetadataBizDictItem.builder()
			.parentItemId(mongo.getParentItemId())
			.itemId(mongo.getItemId())
			.itemName(mongo.getItemName())
			.remark(mongo.getRemark())
			.icon(mongo.getIcon())
			.enabled(mongo.getEnabled())
			.editable(mongo.getEditable())
			.depth(mongo.getDepth())
			.leftNo(mongo.getLeftNo())
			.rightNo(mongo.getRightNo())
			.isSync(mongo.getIsSync())
			.metadata(CairoTenantAppUserConverter.convertTenantAppUser(mongo.getMetadata(), metadataUserMap))
			.build();
	}

	public static BizDict convertBizDict(BizDictMongodb mongo, List<BizDictItemMongodb> sortedItemMongodbList) {
		List<BizDictItem> itemList = sortedItemMongodbList.stream().map(BizDictConverter::convertBizDictItem).collect(Collectors.toList());
		List<BizDictItem> treeItemList = Tree2Converter.build(itemList, ROOT_ID);
		return BizDict.builder()
			.dictId(mongo.getDictId())
			.dictName(mongo.getDictName())
			.icon(mongo.getIcon())
			.enabled(mongo.getEnabled())
			.items(treeItemList)
			.build();
	}

	public static BizDictItem convertBizDictItem(BizDictItemMongodb mongo) {
		return BizDictItem.builder()
			.parentItemId(mongo.getParentItemId())
			.itemId(mongo.getItemId())
			.itemName(mongo.getItemName())
			.remark(mongo.getRemark())
			.icon(mongo.getIcon())
			.enabled(mongo.getEnabled())
			.depth(mongo.getDepth())
			.leftNo(mongo.getLeftNo())
			.rightNo(mongo.getRightNo())
			.build();
	}
}
