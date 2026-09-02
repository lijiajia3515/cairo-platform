package io.github.lijiajia3515.cairo.auth.api.client.dict.biz;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.PathBizDict;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictMongodb;
import io.github.lijiajia3515.cairo.auth.modules.dict.biz.*;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;


@Component
public class BizDictClientApiService {
	private final MongoTemplate readMongoTemplate;

	public BizDictClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}


	@NewSpan
	@BizLog(
		bizId = "bizdict:get_dict_map",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "dictIds", value = "#dictIds"),
		}
	)
	public Map<String, Map<String, BizDictItem>> getBizDictMap(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull Set<String> dictIds) {
		if (dictIds == null || dictIds.isEmpty()) {
			return Collections.emptyMap();
		}

		Criteria bizDictItemCriteria = Criteria
			.where(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).in(dictIds);

		Query bizDictItemQuery = Query.query(bizDictItemCriteria);
		bizDictItemQuery.with(Sort.by(
			Sort.Order.asc(BizDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)
		));

		List<BizDictItemMongodb> bizDictItemMongodbList = readMongoTemplate.find(bizDictItemQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

		return bizDictItemMongodbList.stream()
			.collect(Collectors.groupingBy(
				BizDictItemMongodb::getDictId,
				Collectors.collectingAndThen(Collectors.toList(), g -> g.stream().map(BizDictConverter::convertBizDictItem).collect(Collectors.toMap(BizDictItem::getItemId, x -> x)))
				)
			);
	}

	/**
	 * 查询业务级字典信息
	 *
	 * @param appId        appId
	 * @param dictId dictId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "biz_dict:get_biz_dict_detail_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "dictId", value = "#dictId"),
		}
	)
	public BizDict getBizDictDetailInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @NotNull String dictId) {
		Criteria sdc = Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictMongodb.FIELD.DICT_ID).is(dictId);
		Query sdq = Query.query(sdc);

		BizDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);

		Criteria sdic = Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(dictId);
		Query sdiq = Query.query(sdic);
		sdiq.with(Sort.by(
			Sort.Order.asc(BizDictItemMongodb.FIELD.DEPTH),
			Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)
		));

		List<BizDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiq, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

		if (dictMongodb == null) return null;

		BizDict dict = BizDictConverter.convertBizDict(dictMongodb);
		List<BizDictItem> items = itemMongodbList.stream()
			.map(BizDictConverter::convertBizDictItem)
			.collect(Collectors.toList());

		List<BizDictItem> subItems = Tree2Converter.build(items, ROOT_ID);
		dict.setItems(subItems);
		return dict;
	}

	@NewSpan
	@BizLog(
		bizId = "biz_dict:get_biz_dict_item_id_map",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "itemIdMap", value = "#itemIdMap"),
		}
	)
	public Map<String, Map<String, BizDictItem>> getBizDictItemIdMap(@Valid @NotNull String tenantId,@Valid @NotNull String appId, @Valid @NotNull Map<String, Set<String>> itemIdMap) {
		if (itemIdMap == null || itemIdMap.isEmpty()) {
			return Collections.emptyMap();
		}

		Criteria bizDictItemCriteria = Criteria
			.where(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.APP_ID).is(appId);

		Criteria[] dictItemCriteriaArray = itemIdMap.keySet().stream()
			.filter(x -> !itemIdMap.getOrDefault(x, Collections.emptySet()).isEmpty())
			.map(key -> {
				Criteria criteria1 = Criteria.where(BizDictItemMongodb.FIELD.DICT_ID).is(key);
				Set<String> values = itemIdMap.get(key);
				criteria1.and(BizDictItemMongodb.FIELD.ITEM_ID).in(values);
				return criteria1;
			})
			.toArray(Criteria[]::new);

		bizDictItemCriteria.orOperator(dictItemCriteriaArray);

		Query bizDictItemQuery = Query.query(bizDictItemCriteria);
		bizDictItemQuery.with(Sort.by(
			Sort.Order.asc(BizDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)
		));

		List<BizDictItemMongodb> bizDictItemMongodbList = readMongoTemplate.find(bizDictItemQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

		return bizDictItemMongodbList.stream()
			.collect(Collectors.groupingBy(
				BizDictItemMongodb::getDictId,
				Collectors.collectingAndThen(Collectors.toList(), g -> g.stream().map(BizDictConverter::convertBizDictItem).collect(Collectors.toMap(BizDictItem::getItemId, x -> x)))
				)
			);
	}

	@NewSpan
	@BizLog(
		bizId = "bizDict:get_path_dict_map",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "dictIds", value = "#dictIds"),
		}
	)
	public Map<String, Map<String, PathBizDict>> getPathBizDictMap(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull Set<String> dictIds) {
		if (dictIds == null || dictIds.isEmpty()) {
			return Collections.emptyMap();
		}

		Criteria bizDictItemCriteria = Criteria
			.where(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).in(dictIds);

		Query bizDictItemQuery = Query.query(bizDictItemCriteria);
		bizDictItemQuery.with(Sort.by(
			Sort.Order.asc(BizDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)
		));

		List<BizDictItemMongodb> bizDictItemMongodbList = readMongoTemplate.find(bizDictItemQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);


		List<PathBizDict> pathBizDicts = bizDictItemMongodbList.stream().map(item -> {
			Criteria sdiParentCriteria = Criteria
				.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
				.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(BizDictItemMongodb.FIELD.DICT_ID).is(item.getDictId())
				.and(BizDictItemMongodb.FIELD.LEFT_NO).lt(item.getLeftNo())
				.and(BizDictItemMongodb.FIELD.RIGHT_NO).gt(item.getRightNo());
			Query sdiParentQuery = Query.query(sdiParentCriteria);
			sdiParentQuery.with(Sort.by(Sort.Order.asc(BizDictItemMongodb.FIELD.DEPTH)));
			List<BizDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiParentQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
			itemMongodbList.add(item);
			List<String> itemIds = itemMongodbList.stream().map(BizDictItemMongodb::getItemId).collect(Collectors.toList());
			List<String> itemNames = itemMongodbList.stream().map(BizDictItemMongodb::getItemName).collect(Collectors.toList());
			List<String> itemRemarks = itemMongodbList.stream().map(BizDictItemMongodb::getRemark).collect(Collectors.toList());
			List<String> itemIcons = itemMongodbList.stream().map(BizDictItemMongodb::getIcon).collect(Collectors.toList());
			return PathBizDict.builder()
				.dictId(item.getDictId())
				.itemIds(itemIds)
				.itemNames(itemNames)
				.remarks(itemRemarks)
				.icons(itemIcons)
				.build();
		}).collect(Collectors.toList());

		return pathBizDicts.stream()
			.collect(Collectors.groupingBy(
				PathBizDict::getDictId,
				Collectors.collectingAndThen(Collectors.toList(), g -> g.stream().collect(Collectors.toMap(x -> x.getItemIds().get(x.getItemIds().size() - 1), x -> x)))
				)
			);
	}


	/**
	 * 查询多级字典列表
	 *
	 * @param appId appId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "biz_dict:get_path_biz_dict_item_id_map",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Map<String, Map<String, PathBizDict>> getPathBizDictItemIdMap(String tenantId, String appId, Map<String, Set<String>> itemIdMap) {
		if (itemIdMap == null || itemIdMap.isEmpty()) {
			return Collections.emptyMap();
		}

		Criteria bizDictItemCriteria = Criteria
			.where(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.APP_ID).is(appId);

		Criteria[] dictItemCriteriaArray = itemIdMap.keySet().stream()
			.filter(x -> !itemIdMap.getOrDefault(x, Collections.emptySet()).isEmpty())
			.map(key -> {
				Criteria criteria1 = Criteria.where(BizDictItemMongodb.FIELD.DICT_ID).is(key);
				Set<String> values = itemIdMap.get(key);
				criteria1.and(BizDictItemMongodb.FIELD.ITEM_ID).in(values);
				return criteria1;
			})
			.toArray(Criteria[]::new);

		bizDictItemCriteria.orOperator(dictItemCriteriaArray);

		Query bizDictItemQuery = Query.query(bizDictItemCriteria);
		bizDictItemQuery.with(Sort.by(
			Sort.Order.asc(BizDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)
		));

		List<BizDictItemMongodb> bizDictItemMongodbList = readMongoTemplate.find(bizDictItemQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

		List<PathBizDict> pathBizDicts = bizDictItemMongodbList.stream().map(item -> {
			Criteria sdiParentCriteria = Criteria
				.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
				.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(BizDictItemMongodb.FIELD.DICT_ID).is(item.getDictId())
				.and(BizDictItemMongodb.FIELD.LEFT_NO).lt(item.getLeftNo())
				.and(BizDictItemMongodb.FIELD.RIGHT_NO).gt(item.getRightNo());
			Query sdiParentQuery = Query.query(sdiParentCriteria);
			sdiParentQuery.with(Sort.by(Sort.Order.asc(BizDictItemMongodb.FIELD.DEPTH)));
			List<BizDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiParentQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
			itemMongodbList.add(item);
			List<String> itemIds = itemMongodbList.stream().map(BizDictItemMongodb::getItemId).collect(Collectors.toList());
			List<String> itemNames = itemMongodbList.stream().map(BizDictItemMongodb::getItemName).collect(Collectors.toList());
			List<String> itemRemarks = itemMongodbList.stream().map(BizDictItemMongodb::getRemark).collect(Collectors.toList());
			List<String> itemIcons = itemMongodbList.stream().map(BizDictItemMongodb::getIcon).collect(Collectors.toList());
			return PathBizDict.builder()
				.dictId(item.getDictId())
				.itemIds(itemIds)
				.itemNames(itemNames)
				.remarks(itemRemarks)
				.icons(itemIcons)
				.build();
		}).collect(Collectors.toList());

		return pathBizDicts.stream()
			.collect(Collectors.groupingBy(
				PathBizDict::getDictId,
				Collectors.collectingAndThen(Collectors.toList(), g -> g.stream().collect(Collectors.toMap(x -> x.getItemIds().get(x.getItemIds().size() - 1), x -> x)))
				)
			);
	}

}
