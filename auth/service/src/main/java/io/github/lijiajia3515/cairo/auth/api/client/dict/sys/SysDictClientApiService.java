package io.github.lijiajia3515.cairo.auth.api.client.dict.sys;


import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.PathSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.modules.dict.sys.SysDictConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * client-api dict service
 */
@Slf4j
@Validated
@Component
public class SysDictClientApiService {
	private final MongoTemplate readMongoTemplate;

	SysDictClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_dict_map",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "dictIds", value = "#dictIds"),
		}
	)
	public Map<String, Map<String, SysDictItem>> getSysDictMap(@Valid @NotNull String appId, @Valid @NotNull Set<String> dictIds) {
		if (dictIds == null || dictIds.isEmpty()) {
			return Collections.emptyMap();
		}

		Criteria sysDictItemCriteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).in(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).in(dictIds);

		Query sysDictItemQuery = Query.query(sysDictItemCriteria);
		sysDictItemQuery.with(Sort.by(
			Sort.Order.asc(SysDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)
		));

		List<SysDictItemMongodb> sysDictItemMongodbList = readMongoTemplate.find(sysDictItemQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		return sysDictItemMongodbList.stream()
			.collect(Collectors.groupingBy(
					SysDictItemMongodb::getDictId,
					Collectors.collectingAndThen(Collectors.toList(), g -> g.stream().map(SysDictConverter::convertSysDictItem).collect(Collectors.toMap(SysDictItem::getItemId, x -> x)))
				)
			);
	}

	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_item_id_map",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "itemIdMap", value = "#itemIdMap"),
		}
	)
	public Map<String, Map<String, SysDictItem>> getSysDictItemIdMap(@Valid @NotNull String appId, @Valid @NotNull Map<String, Set<String>> itemIdMap) {
		if (itemIdMap == null || itemIdMap.isEmpty()) {
			return Collections.emptyMap();
		}

		Criteria sysDictItemCriteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).in(appId);

		Criteria[] dictItemCriteriaArray = itemIdMap.keySet().stream()
			.filter(x -> !itemIdMap.getOrDefault(x, Collections.emptySet()).isEmpty())
			.map(key -> {
				Criteria criteria1 = Criteria.where(SysDictItemMongodb.FIELD.DICT_ID).is(key);
				Set<String> values = itemIdMap.get(key);
				criteria1.and(SysDictItemMongodb.FIELD.ITEM_ID).in(values);
				return criteria1;
			})
			.toArray(Criteria[]::new);

		sysDictItemCriteria.orOperator(dictItemCriteriaArray);

		Query sysDictItemQuery = Query.query(sysDictItemCriteria);
		sysDictItemQuery.with(Sort.by(
			Sort.Order.asc(SysDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)
		));

		List<SysDictItemMongodb> sysDictItemMongodbList = readMongoTemplate.find(sysDictItemQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		return sysDictItemMongodbList.stream()
			.collect(Collectors.groupingBy(
					SysDictItemMongodb::getDictId,
					Collectors.collectingAndThen(Collectors.toList(), g -> g.stream().map(SysDictConverter::convertSysDictItem).collect(Collectors.toMap(SysDictItem::getItemId, x -> x)))
				)
			);

	}

	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "itemIdMap", value = "#itemIdMap"),
		}
	)
    SysDict getSysDictDetailInfo(@Valid @NotNull String appId, @NotNull String dictId,Boolean itemEnabled) {
		Criteria sdCriteria = Criteria.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(dictId);
		Query sdq = Query.query(sdCriteria);
		SysDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);


		Criteria sdiCriteria = Criteria.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(dictId);
		if (itemEnabled!=null){
			sdiCriteria.and(SysDictItemMongodb.FIELD.ENABLED).is(itemEnabled);
		}

		Query sdiq = Query.query(sdiCriteria);
		sdiq.with(Sort.by(
			Sort.Order.asc(SysDictItemMongodb.FIELD.DEPTH),
			Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)
		));

		List<SysDictItemMongodb> sysDictItemMongodbList = readMongoTemplate.find(sdiq, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		return Optional.ofNullable(dictMongodb)
			.map(d -> {
				SysDict sysDict = SysDictConverter.convertSysDict(d);
				sysDict.setItems(sysDictItemMongodbList.stream().map(SysDictConverter::convertSysDictItem).collect(Collectors.toList()));
				return sysDict;
			}).orElse(null);
	}


	@NewSpan
	@BizLog(
		bizId = "sysDict:get_path_dict_map",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "dictIds", value = "#dictIds"),
		}
	)
	public Map<String, Map<String, PathSysDict>> getPathSysDictMap(@Valid @NotNull String appId, @Valid @NotNull Set<String> dictIds) {
		if (dictIds == null || dictIds.isEmpty()) {
			return Collections.emptyMap();
		}

		Criteria sysDictItemCriteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).in(dictIds);

		Query sysDictItemQuery = Query.query(sysDictItemCriteria);
		sysDictItemQuery.with(Sort.by(
			Sort.Order.asc(SysDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)
		));

		List<SysDictItemMongodb> sysDictItemMongodbList = readMongoTemplate.find(sysDictItemQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);


		List<PathSysDict> pathSystemDicts = sysDictItemMongodbList.stream().map(item -> {
			Criteria sdiParentCriteria = Criteria
				.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
				.and(SysDictItemMongodb.FIELD.DICT_ID).is(item.getDictId())
				.and(SysDictItemMongodb.FIELD.LEFT_NO).lt(item.getLeftNo())
				.and(SysDictItemMongodb.FIELD.RIGHT_NO).gt(item.getRightNo());
			Query sdiParentQuery = Query.query(sdiParentCriteria);
			sdiParentQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.DEPTH)));
			List<SysDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiParentQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
			itemMongodbList.add(item);
			List<String> itemIds = itemMongodbList.stream().map(SysDictItemMongodb::getItemId).collect(Collectors.toList());
			List<String> itemNames = itemMongodbList.stream().map(SysDictItemMongodb::getItemName).collect(Collectors.toList());
			List<String> itemRemarks = itemMongodbList.stream().map(SysDictItemMongodb::getRemark).collect(Collectors.toList());
			List<String> itemIcons = itemMongodbList.stream().map(SysDictItemMongodb::getIcon).collect(Collectors.toList());
			return PathSysDict.builder()
				.dictId(item.getDictId())
				.itemIds(itemIds)
				.itemNames(itemNames)
				.remarks(itemRemarks)
				.icons(itemIcons)
				.build();
		}).collect(Collectors.toList());

		return pathSystemDicts.stream()
			.collect(Collectors.groupingBy(
					PathSysDict::getDictId,
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
		bizId = "sys_dict:get_path_sys_dict_item_id_map",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Map<String, Map<String, PathSysDict>> getPathSysDictItemIdMap(String appId, Map<String, Set<String>> itemIdMap) {
		if (itemIdMap == null || itemIdMap.isEmpty()) {
			return Collections.emptyMap();
		}

		Criteria sysDictItemCriteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId);

		Criteria[] dictItemCriteriaArray = itemIdMap.keySet().stream()
			.filter(x -> !itemIdMap.getOrDefault(x, Collections.emptySet()).isEmpty())
			.map(key -> {
				Criteria criteria1 = Criteria.where(SysDictItemMongodb.FIELD.DICT_ID).is(key);
				Set<String> values = itemIdMap.get(key);
				criteria1.and(SysDictItemMongodb.FIELD.ITEM_ID).in(values);
				return criteria1;
			})
			.toArray(Criteria[]::new);

		sysDictItemCriteria.orOperator(dictItemCriteriaArray);

		Query sysDictItemQuery = Query.query(sysDictItemCriteria);
		sysDictItemQuery.with(Sort.by(
			Sort.Order.asc(SysDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)
		));

		List<SysDictItemMongodb> sysDictItemMongodbList = readMongoTemplate.find(sysDictItemQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		List<PathSysDict> pathSystemDicts = sysDictItemMongodbList.stream().map(item -> {
			Criteria sdiParentCriteria = Criteria
				.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
				.and(SysDictItemMongodb.FIELD.DICT_ID).is(item.getDictId())
				.and(SysDictItemMongodb.FIELD.LEFT_NO).lt(item.getLeftNo())
				.and(SysDictItemMongodb.FIELD.RIGHT_NO).gt(item.getRightNo());
			Query sdiParentQuery = Query.query(sdiParentCriteria);
			sdiParentQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.DEPTH)));
			List<SysDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiParentQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
			itemMongodbList.add(item);
			List<String> itemIds = itemMongodbList.stream().map(SysDictItemMongodb::getItemId).collect(Collectors.toList());
			List<String> itemNames = itemMongodbList.stream().map(SysDictItemMongodb::getItemName).collect(Collectors.toList());
			List<String> itemRemarks = itemMongodbList.stream().map(SysDictItemMongodb::getRemark).collect(Collectors.toList());
			List<String> itemIcons = itemMongodbList.stream().map(SysDictItemMongodb::getIcon).collect(Collectors.toList());
			return PathSysDict.builder()
				.dictId(item.getDictId())
				.itemIds(itemIds)
				.itemNames(itemNames)
				.remarks(itemRemarks)
				.icons(itemIcons)
				.build();
		}).collect(Collectors.toList());

		return pathSystemDicts.stream()
			.collect(Collectors.groupingBy(
					PathSysDict::getDictId,
					Collectors.collectingAndThen(Collectors.toList(), g -> g.stream().collect(Collectors.toMap(x -> x.getItemIds().get(x.getItemIds().size() - 1), x -> x)))
				)
			);
	}

	/**
	 * 查询字典项列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_sub_item_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<SysDictItem> getSysDictSubItemList(@Valid @NotNull String appId, @Validated GetSysDictSubItemArgs args) {
		Criteria sdc = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(args.getDictId());
		Query sdq = Query.query(sdc);

		SysDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
		if (dictMongodb == null) return null;

		Criteria sdic = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(SysDictItemMongodb.FIELD.PARENT_ITEM_ID).is(args.getParentItemId());
		Query sdiq = Query.query(sdic);
		sdiq.with(Sort.by(
			Sort.Order.asc(SysDictItemMongodb.FIELD.DEPTH),
			Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)
		));

		List<SysDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiq, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		return itemMongodbList.stream()
			.map(SysDictConverter::convertSysDictItem)
			.collect(Collectors.toList());
	}
}
