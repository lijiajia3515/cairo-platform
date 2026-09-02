package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.dict.sys;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.sys.GetSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.sys.GetSysDictDetailListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.sys.GetSysDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictMongodb;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;

/**
 * [tenant_subapp_user/api]tenant app subapp system dict service
 */
@Slf4j
@Validated
@Component
public class SysDictTenantSubappApiService {
	private final MongoTemplate readMongoTemplate;


	SysDictTenantSubappApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 查询系统级字典列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<SysDict> getSysDictList(@Valid @NotNull String appId, @Validated GetSysDictArgs args) {
		Criteria sdc = Criteria.where(SysDictMongodb.FIELD.APP_ID).is(appId);
		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			sdc.and(SysDictMongodb.FIELD.DICT_NAME).regex(args.getKeyword());
		}
		if (args.getDictIds() != null && !args.getDictIds().isEmpty()) {
			sdc.and(SysDictMongodb.FIELD.DICT_ID).in(args.getDictIds());
		}

		Query query = Query.query(sdc);
		query.with(Sort.by(Sort.Order.desc(SysDictMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<SysDictMongodb> sysDictMongodbList = readMongoTemplate.find(query, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
		return getSysDictList(sysDictMongodbList);
	}

	/**
	 * 查询系统级字典分页列表
	 *
	 * @param appId appId
	 * @param args  query args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<SysDict> getSysDictPageList(@NotNull String appId, @Validated GetSysDictArgs args) {
		Criteria sdc = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId);
		if (args.getKeyword() != null) {
			sdc.and(SysDictMongodb.FIELD.DICT_NAME).regex(args.getKeyword());
		}
		if (args.getDictIds() != null && !args.getDictIds().isEmpty()) {
			sdc.and(SysDictMongodb.FIELD.DICT_ID).in(args.getDictIds());
		}

		Query query = Query.query(sdc);
		long total = readMongoTemplate.count(query, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(SysDictMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<SysDictMongodb> sysDictMongodbList = readMongoTemplate.find(query, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
		List<SysDict> contents = getSysDictList(sysDictMongodbList);
		return new Page<>(args, contents, total);
	}

	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_detail_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<SysDict> getSysDictDetailList(@Valid @NotNull String appId, @Validated GetSysDictDetailListArgs args) {
		Criteria sdc = Criteria.where(SysDictMongodb.FIELD.APP_ID).is(appId);
		sdc.and(SysDictMongodb.FIELD.DICT_ID).in(args.getDictIds());

		Query query = Query.query(sdc);
		query.with(Sort.by(Sort.Order.desc(SysDictMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<SysDictMongodb> sysDictMongodbList = readMongoTemplate.find(query, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
		if (sysDictMongodbList.isEmpty()) return Collections.emptyList();

		List<String> dictIds = sysDictMongodbList.stream().map(SysDictMongodb::getDictId).collect(Collectors.toList());
		Criteria sdic = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).in(dictIds);

		Query sdiq = Query.query(sdic);
		sdiq.with(Sort.by(
			Sort.Order.asc(SysDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(SysDictItemMongodb.FIELD.DEPTH),
			Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)
		));

		List<SysDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiq, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		Map<String, List<SysDictItemMongodb>> dictItemMongodbListMap = itemMongodbList.stream().collect(Collectors.groupingBy(SysDictItemMongodb::getDictId));

		return getSysDictList(sysDictMongodbList, dictItemMongodbListMap);
	}

	/**
	 * 查询系统级字典信息
	 *
	 * @param appId        appId
	 * @param dictId dictId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "dictId", value = "#dictId"),
		}
	)
	public SysDict getSysDictInfo(@Valid @NotNull String appId, @NotNull String dictId) {
		Criteria sdc = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(dictId);
		Query sdq = Query.query(sdc);

		SysDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);

		if (dictMongodb == null) return null;
		return SysDictConverter.convertSysDict(dictMongodb);
	}

	/**
	 * 查询系统级字典详细信息
	 *
	 * @param appId        appId
	 * @param dictId dictId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_detail_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "dictId", value = "#dictId"),
		}
	)
	public SysDict getSysDictDetailInfo(@Valid @NotNull String appId, @NotNull String dictId) {
		Criteria sdc = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(dictId);
		Query sdq = Query.query(sdc);

		SysDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
		if (dictMongodb == null) return null;

		Criteria sdic = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(dictId);
		Query sdiq = Query.query(sdic);
		sdiq.with(Sort.by(
			Sort.Order.asc(SysDictItemMongodb.FIELD.DEPTH),
			Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)
		));

		List<SysDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiq, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		if (itemMongodbList.isEmpty()) return null;

		SysDict dict = SysDictConverter.convertSysDict(dictMongodb);
		List<SysDictItem> items = itemMongodbList.stream()
			.map(SysDictConverter::convertSysDictItem)
			.collect(Collectors.toList());

		List<SysDictItem> subItems = Tree2Converter.build(items, ROOT_ID);
		dict.setItems(subItems);
		return dict;
	}

	/**
	 * 查询系统级字典项信息
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_item_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<SysDictItem> getSysDictItemInfo(@Valid @NotNull String appId, @Validated GetSysDictItemInfoArgs args) {
		Criteria sdiCriteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(SysDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId());
		Query sdiQuery = Query.query(sdiCriteria);
		SysDictItemMongodb itemMongodb = readMongoTemplate.findOne(sdiQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		if (itemMongodb == null) return Collections.emptyList();

		Criteria sdiParentCriteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(SysDictItemMongodb.FIELD.LEFT_NO).lt(itemMongodb.getLeftNo())
			.and(SysDictItemMongodb.FIELD.RIGHT_NO).gt(itemMongodb.getRightNo());
		Query sdiParentQuery = Query.query(sdiParentCriteria);
		sdiParentQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.DEPTH)));

		List<SysDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiParentQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
		itemMongodbList.add(itemMongodb);

		return itemMongodbList.stream()
			.map(SysDictConverter::convertSysDictItem)
			.collect(Collectors.toList());
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

	/**
	 * 查询字典项列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_sub_item_tree_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<SysDictItem> getSysDictSubItemTreeList(@Valid @NotNull String appId, @Validated GetSysDictSubItemArgs args) {
		Criteria sdc = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(args.getDictId());
		Query sdq = Query.query(sdc);

		SysDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
		if (dictMongodb == null) return Collections.emptyList();
		int leftNo, rightNo;
		if (!ROOT_ID.equals(args.getParentItemId())) {
			Criteria sdic = Criteria
				.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
				.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
				.and(SysDictItemMongodb.FIELD.PARENT_ITEM_ID).is(args.getParentItemId());
			Query sdiq = Query.query(sdic);

			SysDictItemMongodb parentItemMongodb = readMongoTemplate.findOne(sdiq, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
			if (parentItemMongodb != null) {
				leftNo = parentItemMongodb.getLeftNo();
				rightNo = parentItemMongodb.getRightNo();
			} else {
				return Collections.emptyList();
			}
		} else {
			leftNo = dictMongodb.getLeftNo();
			rightNo = dictMongodb.getRightNo();
		}

		Criteria sdic = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(SysDictItemMongodb.FIELD.LEFT_NO).gte(leftNo)
			.and(SysDictItemMongodb.FIELD.RIGHT_NO).lte(rightNo);
		Query sdiq = Query.query(sdic);
		sdiq.with(Sort.by(
			Sort.Order.asc(SysDictItemMongodb.FIELD.DEPTH),
			Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)
		));
		List<SysDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiq, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		List<SysDictItem> subItems = itemMongodbList.stream()
			.map(SysDictConverter::convertSysDictItem)
			.collect(Collectors.toList());

		return Tree2Converter.build(subItems, args.getParentItemId());
	}

	private List<SysDict> getSysDictList(List<SysDictMongodb> mongodbList) {
		return mongodbList.stream()
			.map(SysDictConverter::convertSysDict)
			.collect(Collectors.toList());
	}

	private List<SysDict> getSysDictList(List<SysDictMongodb> mongodbList, Map<String, List<SysDictItemMongodb>> itemMap) {
		return mongodbList.stream()
			.map(x -> SysDictConverter.convertSysDict(x, itemMap.get(x.getDictId())))
			.collect(Collectors.toList());
	}

}
