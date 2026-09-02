package io.github.lijiajia3515.cairo.auth.api.subapp.dict.sys;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.api.client.account.AccountClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.*;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.CopySysDictByDictIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.DeleteSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.DeleteSysDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.DictType;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictDetailListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictItemPageInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifyAppUserSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifyAppUserSysDictIconArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifyAppUserSysDictItemIconArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifyAppUserSysDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifySysDictItemStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.MoveSysDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.PutAppUserSysDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.SyncSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.modules.dict.sys.SysDictConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import io.github.lijiajia3515.cairo.auth.modules.dict.sys.common.SysDictCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.dict.sys.DeleteSysDictMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.dict.sys.SyncSysDictMessage;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileCommonService;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants;

/**
 * [subapp_user/api] system dict service
 */
@Slf4j
@Validated
@Component
public class SysDictSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final PublicFileCommonService publicFileCommonService;
	private final SysDictCommonService sysDictCommonService;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;
	protected final AccountClientApiService accountClientApiService;

	SysDictSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
											 TransactionTemplate transactionTemplate,
											 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
											 PublicFileCommonService publicFileCommonService,
											 SysDictCommonService sysDictCommonService,
											 RabbitTemplate rabbitTemplate,
											 CairoRabbitmqTool cairoRabbitmqTool,
											 ObjectMapper objectMapper,
											 AccountClientApiService accountClientApiService) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.publicFileCommonService = publicFileCommonService;
		this.sysDictCommonService = sysDictCommonService;
		this.objectMapper = objectMapper;
		this.accountClientApiService = accountClientApiService;
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
	public List<MetadataSysDict> getSysDictList(@Valid @NotNull String appId, @Validated GetSysDictArgs args) {
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
		return getSysDictList(appId, sysDictMongodbList);
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
	public Page<MetadataSysDict> getSysDictPageList(@NotNull String appId, @Validated GetSysDictArgs args) {
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
		List<MetadataSysDict> contents = getSysDictList(appId, sysDictMongodbList);
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
	public SysDict getSysDictDetailInfo(@Valid @NotNull String appId, @NotNull String dictId, Boolean itemEnabled) {
		Criteria sdc = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(dictId);
		Query sdq = Query.query(sdc);

		SysDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
		if (dictMongodb == null) return null;

		Criteria sdic = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(dictId);
		if (itemEnabled != null) {
			sdic.and(SysDictItemMongodb.FIELD.ENABLED).is(itemEnabled);
		}
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
	 * 查询系统级字典项分页信息
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sys_dict:get_sys_dict_item_page_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataSysDictItem> getSysDictItemPageList(String appId, GetSysDictItemPageInfoArgs args) {
		Criteria criteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(SysDictItemMongodb.FIELD.PARENT_ITEM_ID).is(args.getParentItemId());

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.by(SysDictItemMongodb.FIELD.LEFT_NO)));
		long total = readMongoTemplate.count(query, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
		query.with(args.pageable());
		List<SysDictItemMongodb> dictItemMongodbs = readMongoTemplate.find(query, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(dictItemMongodbs.stream().map(SysDictItemMongodb::getMetadata).collect(Collectors.toList()));

		Map<String, Account> accountMap = accountClientApiService.getAccountList(GetAccountListArgs.builder().accountIds(metadataAccountIds).build()).stream().collect(Collectors.toMap(Account::getAccountId, g->g));

		List<MetadataSysDictItem> dictItems = dictItemMongodbs.stream()
			.map(x -> SysDictConverter.convertMetadataSysDictItem(x, accountMap))
			.collect(Collectors.toList());
		return new Page<>(args, dictItems, total);
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

	/**
	 * 修改系统级字典
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_sys_dict_info", keys = {"#appId","#args.dictId"})
	@BizLog(
		bizId = "sys_dict:modify_sys_dict_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifySysDictInfo(@Valid @NotNull String appId, @Validated ModifyAppUserSysDictArgs args) {
		DictType type = DictType.typeValueOf(args.getDictType()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 类型：%s 错误", args.getDictType())));
		Criteria criteria = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(args.getDictId());

		Query query = Query.query(criteria);

		Update update = new Update();
		if (args.getDictName() != null) {
			update.set(SysDictMongodb.FIELD.DICT_NAME, args.getDictName());
		}
		if (args.getIsCreateItem() != null) {
			update.set(SysDictMongodb.FIELD.IS_CREATE_ITEM, args.getIsCreateItem());
		}
		if (args.getDictType() != null) {
			update.set(SysDictMongodb.FIELD.DICT_TYPE, type.getTypeValue());
		}
		update.set(SysDictMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
		update.currentDate(SysDictMongodb.FIELD.METADATA.UPDATE_TIME);

		UpdateResult updateResult = transactionTemplate.execute(transactionStatus -> {
			try {
				return mongoTemplate.updateFirst(query, update, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
			} catch (Exception e) {
				log.debug("modifySysDictInfo", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改系统级字典信息失败");
			}
		});
		if (updateResult == null || updateResult.getModifiedCount() != 1) {
			throw new ConflictBusinessException("修改系统级字典信息失败");
		}
	}

	/**
	 * 修改系统级字典图标
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_sys_dict_icon", keys = {"#appId","#args.dictId"})
	@BizLog(
		bizId = "sys_dict:modify_sys_dict_icon",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifySysDictIcon(String appId, ModifyAppUserSysDictIconArgs args) {
		Criteria criteria = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(args.getDictId());

		Query query = Query.query(criteria);

		Update update = new Update();
		if (args.getIcon() != null && !args.getIcon().isBlank()) {
			String icon = null;
			String fileName = CoreConstants.SNOWFLAKE.nextIdStr();
			MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(args.getIcon(),
				fileName.concat(FilesUtil.getType(args.getIcon())));
			if (multipartFile != null) {
				List<String> urls = publicFileCommonService.uploadFile(appId
						.concat("/")
						.concat(FileKeyPrefixConstants.Collection.SYS_DICT_ICON)
						.concat("/")
						.concat(fileName)
						.concat(FilesUtil.getType((FilesUtil.getType(args.getIcon())))),
					multipartFile);
				if (urls.size() > 2) icon = urls.get(2);
			}
			update.set(SysDictMongodb.FIELD.ICON, icon);
		} else {
			update.set(SysDictMongodb.FIELD.ICON, null);
		}
		update.set(SysDictMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
		update.currentDate(SysDictMongodb.FIELD.METADATA.UPDATE_TIME);

		UpdateResult updateResult = transactionTemplate.execute(transactionStatus -> {
			try {
				return mongoTemplate.updateFirst(query, update, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
			} catch (Exception e) {
				log.debug("modifySysDictIcon", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改系统级字典图标失败");
			}
		});
		if (updateResult == null || updateResult.getModifiedCount() != 1) {
			throw new ConflictBusinessException("修改系统级字典图标失败");
		}
	}

	/**
	 * 添加系统级字典项
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "sys_dict:put_sys_dict_item",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void putSysDictItem(@Valid @NotNull String appId, @Validated PutAppUserSysDictItemArgs args) {
		String dictId = args.getDictId();

		transactionTemplate.executeWithoutResult(status -> {
			try {
				String parentItemId = Optional.ofNullable(args.getParentItemId()).orElse(ROOT_ID);
				Query parentQuery = Query.query(Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(dictId)
					.and(SysDictItemMongodb.FIELD.ITEM_ID).is(parentItemId)
				);
				SysDictItemMongodb parentItem = mongoTemplate.findOne(parentQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				if (parentItem == null && ROOT_ID.equals(parentItemId)) {
					parentItem = sysDictCommonService.getRootItem(appId, dictId);
				}

				if (parentItem == null) {
					throw new ConflictBusinessException("系统级字典项ParentItemId错误");
				}

				// find brother
				Query brotherNodeQuery = Query.query(
					Criteria.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(dictId)
						.and(SysDictItemMongodb.FIELD.PARENT_ITEM_ID).is(parentItemId)
				);
				brotherNodeQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)));

				List<SysDictItemMongodb> brotherNodes = mongoTemplate.find(brotherNodeQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				Optional<SysDictItemMongodb> afterNode = brotherNodes.stream().filter(a -> a.getItemId().equals(args.getBeforeItemId())).findFirst();
				int position;
				int left;
				int right;
				if (brotherNodes.isEmpty() || afterNode.isEmpty()) {
					position = parentItem.getRightNo();
					left = parentItem.getRightNo();
					right = parentItem.getRightNo() + 1;
				} else {
					position = afterNode.get().getLeftNo();
					left = afterNode.get().getLeftNo();
					right = afterNode.get().getLeftNo() + 1;
				}


				// 左值扩容
				Query leftParentQuery = Query.query(Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(dictId)
					.and(SysDictItemMongodb.FIELD.LEFT_NO).gte(position)
				);
				leftParentQuery.with(Sort.by(Sort.Order.desc(SysDictItemMongodb.FIELD.LEFT_NO)));
				List<SysDictItemMongodb> leftNodes = mongoTemplate.find(leftParentQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				leftNodes.forEach(x -> {
					Query query = Query.query(Criteria
						.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(dictId)
						.and(SysDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
					);
					Update update = new Update()
						.inc(SysDictItemMongodb.FIELD.LEFT_NO, 2)
						.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult updateResult = mongoTemplate.updateFirst(query, update, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					log.debug("update system dict item left values to db : [{}]", updateResult);
				});

				// 右值扩容
				Query rightParentQuery = Query.query(Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(dictId)
					.and(SysDictItemMongodb.FIELD.RIGHT_NO).gte(position)
				);
				rightParentQuery.with(Sort.by(Sort.Order.desc(SysDictItemMongodb.FIELD.RIGHT_NO)));
				List<SysDictItemMongodb> moveRightNodes = mongoTemplate.find(rightParentQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				moveRightNodes.forEach(x -> {
					Query query = Query.query(Criteria
						.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(dictId)
						.and(SysDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
					);
					Update update = new Update()
						.inc(SysDictItemMongodb.FIELD.RIGHT_NO, 2)
						.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult updateResult = mongoTemplate.updateFirst(query, update, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					log.debug("update system dict item right values to db : [{}]", updateResult);
					// TODO 更新字典表 left,right
				});

				// 更新root右值
				Query sysDictQuery = Query.query(Criteria
					.where(SysDictMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictMongodb.FIELD.DICT_ID).is(dictId)
				);
				Update sysDictUpdate = new Update()
					.inc(SysDictMongodb.FIELD.RIGHT_NO, 2)
					.currentDate(SysDictMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult updateResult = mongoTemplate.updateFirst(sysDictQuery, sysDictUpdate, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
				log.debug("update system dict right values to db : [{}]", updateResult);

				// 插入字典项
				SysDictItemMongodb itemMongodb = SysDictItemMongodb.builder()
					.appId(appId)
					.dictId(dictId)
					.parentItemId(parentItemId)
					.itemId(args.getItemId())
					.itemName(args.getItemName())
					.editable(Optional.ofNullable(args.getEditable()).orElse(true))
					.enabled(Optional.ofNullable(args.getEnabled()).orElse(true))
					.remark(args.getRemark())
					.leftNo(left)
					.rightNo(right)
					.depth(parentItem.getDepth() + 1)
					.metadata(AccountMetadataMongodb.builder()
						.createAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.updateAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.build())
					.build();
				SysDictItemMongodb insert = mongoTemplate.insert(itemMongodb, MongodbConstants.Collection.SYS_DICT_ITEM);
				log.info("insert system dict item to db : {}", insert.getItemId());
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.info("putSysDictItem", e);
				throw new ConflictBusinessException("添加系统级字典项失败");
			}
		});

	}

	/**
	 * 修改系级集字典项
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_sys_dict_item_info", keys = {"#appId","#args.dictId","#args.itemId"})
	@BizLog(
		bizId = "dict:modify_sys_dict_item_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifySysDictItemInfo(@Valid @NotNull String appId, @Validated ModifyAppUserSysDictItemInfoArgs args) {
		UpdateResult result = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId());
				Query query = Query.query(criteria);
				SysDictItemMongodb node = mongoTemplate.findOne(query, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

				if (node == null) {
					throw new ConflictBusinessException("更新系统级字典项失败，系统级字典项不存在");
				}

				Update update = new Update();
				if (args.getItemName() != null) {
					update.set(SysDictItemMongodb.FIELD.ITEM_NAME, args.getItemName());
				}

				if (args.getEditable() != null) {
					update.set(SysDictItemMongodb.FIELD.EDITABLE, args.getEditable());
				}

				if (args.getRemark() != null) {
					update.set(SysDictItemMongodb.FIELD.REMARK, args.getRemark());
				}


				update.set(SysDictItemMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());

				update.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateFirst = mongoTemplate.updateFirst(query, update, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

				return updateFirst;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifySysDictItemInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改系统级字典项失败");
			}
		});

		if (result == null || result.getModifiedCount() < 1) {
			throw new ConflictBusinessException("修改系统级字典项失败");
		}
	}

	/**
	 * 修改系级级字典项图标
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_sys_dict_item_icon", keys = {"#appId","#args.dictId","#args.itemId"})
	@BizLog(
		bizId = "dict:modify_sys_dict_item_icon",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifySysDictItemIcon(@Valid @NotNull String appId, @Validated ModifyAppUserSysDictItemIconArgs args) {
		UpdateResult result = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId());
				Query query = Query.query(criteria);
				SysDictItemMongodb node = mongoTemplate.findOne(query, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

				if (node == null) {
					throw new ConflictBusinessException("更新系统级字典项失败，系统级字典项不存在");
				}

				Update update = new Update();

				if (args.getIcon() != null) {
					String icon = null;
					String fileName = CoreConstants.SNOWFLAKE.nextIdStr();
					MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(args.getIcon(),
						fileName.concat(FilesUtil.getType(args.getIcon())));
					if (multipartFile != null) {
						List<String> urls = publicFileCommonService.uploadFile(appId
								.concat("/")
								.concat(FileKeyPrefixConstants.Collection.SYS_DICT_ITEM_ICON)
								.concat("/")
								.concat(fileName)
								.concat(FilesUtil.getType((FilesUtil.getType(args.getIcon())))),
							multipartFile);
						if (urls.size() > 2) icon = urls.get(2);
					}
					update.set(SysDictItemMongodb.FIELD.ICON,icon);
				} else {
					update.set(SysDictItemMongodb.FIELD.ICON, null);
				}

				update.set(SysDictItemMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());

				update.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateFirst = mongoTemplate.updateFirst(query, update, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				return updateFirst;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifySysDictItemIcon", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改系统级字典项图标失败");
			}
		});

		if (result == null || result.getModifiedCount() < 1) {
			throw new ConflictBusinessException("修改系统级字典项图标失败");
		}
	}

	/**
	 * 修改系级集字典状态
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_sys_dict_item_status", keys = {"#appId","#args.dictId","#args.itemId"})
	@BizLog(
		bizId = "dict:modify_sys_dict_item_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifySysDictItemStatus(@Valid @NotNull String appId, @Validated ModifySysDictItemStatusArgs args) {
		UpdateResult result = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId());
				Query query = Query.query(criteria);
				SysDictItemMongodb node = mongoTemplate.findOne(query, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

				if (node == null) {
					throw new ConflictBusinessException("更新系统级字典项状态失败，系统级字典项不存在");
				}

				Update update = new Update();
				if (args.getEnabled() != null) {
					update.set(SysDictItemMongodb.FIELD.ENABLED, args.getEnabled());
				}

				update.set(SysDictItemMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());

				update.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateFirst = mongoTemplate.updateFirst(query, update, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				return updateFirst;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifySysDictItemStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改系统级字典项状态失败");
			}
		});

		if (result == null || result.getModifiedCount() < 1) {
			throw new ConflictBusinessException("修改系统级字典项状态失败");
		}
	}

	/**
	 * 移动系级级字典
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "move_sys_dict_item", keys = {"#appId","#args.dictId"})
	@BizLog(
		bizId = "dict:move_sys_dict_item",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void moveSysDictItem(@Valid @NotNull String appId, @Validated MoveSysDictItemArgs args) {
		// 	1. 删除移动的节点
		// 	2. 缩容
		//  3. 扩容
		//  4. 插入删移动的节点（更新leftNo,rightNo)
		transactionTemplate.executeWithoutResult(status -> {
			try {
				// 先查询三个节点信息
				Criteria criteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())

					.and(SysDictItemMongodb.FIELD.ITEM_ID).in(args.getMoveItemId(), args.getBeforeItemId(), args.getParentItemId());
				Query query = Query.query(criteria);
				Map<String, SysDictItemMongodb> nodeIdMap = mongoTemplate.find(query, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM).stream().collect(Collectors.toMap(SysDictItemMongodb::getItemId, x -> x));
				// 移动的节点
				SysDictItemMongodb moveNode = nodeIdMap.get(args.getMoveItemId());
				// 移动后的父节点
				SysDictItemMongodb parentNode;
				if (CairoAuthConstants.ROOT_ID.equals(args.getParentItemId())) {
					parentNode = sysDictCommonService.getRootItem(appId, args.getDictId());
				} else {
					parentNode = nodeIdMap.get(args.getParentItemId());
				}
				// 移动后的左边节点
				SysDictItemMongodb beforeNode = nodeIdMap.get(args.getBeforeItemId());
				if (moveNode == null || parentNode == null) {
					throw new ConflictBusinessException("moveItemId is null or parentItemId 错误");
				}

				if ((parentNode.getLeftNo() >= moveNode.getLeftNo() && parentNode.getRightNo() <= moveNode.getRightNo())) {
					throw new ConflictBusinessException("parentItemId 不能设置为移动节点的子节点");
				}


				// 容错beforeId错误的问题，默认移动到最后
				if (beforeNode != null && !beforeNode.getParentItemId().equals(parentNode.getItemId())) {
					beforeNode = null;
				}

				// 查询后删除移动的节点
				Criteria moveNodeCriteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.LEFT_NO).gte(moveNode.getLeftNo())
					.and(SysDictItemMongodb.FIELD.RIGHT_NO).lte(moveNode.getRightNo());
				Query moveNodeQuery = Query.query(moveNodeCriteria);
				moveNodeQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)));
				List<SysDictItemMongodb> moveNodes = mongoTemplate.findAllAndRemove(moveNodeQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				// 移动的数值
				int moveNum = moveNodes.size() * 2;

				// 缩容左值
				Criteria subNodeLeftCriteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.LEFT_NO).gte(moveNode.getRightNo() + 1);
				Query subNodeLeftQuery = Query.query(subNodeLeftCriteria);
				subNodeLeftQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)));
				List<SysDictItemMongodb> subNodeLeftNodes = mongoTemplate.find(subNodeLeftQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

				Update subNodeLeftUpdate = new Update()
					.inc(SysDictItemMongodb.FIELD.LEFT_NO, -moveNum)
					.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
				subNodeLeftNodes.forEach(x -> {
					Query subNodeLeftSubQuery = Query.query(Criteria
						.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
						.and(SysDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId()));
					UpdateResult addNodeLeftUpdateResult = mongoTemplate.updateFirst(subNodeLeftSubQuery, subNodeLeftUpdate, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					log.info("addNodeLeftUpdateResult: {}", addNodeLeftUpdateResult);
				});

				// 缩容右值
				Criteria subNodeRightCriteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.RIGHT_NO).gte(moveNode.getRightNo() + 1);
				Query subNodeRightQuery = Query.query(subNodeRightCriteria);
				subNodeRightQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.RIGHT_NO)));
				List<SysDictItemMongodb> subNodeRightNodes = mongoTemplate.find(subNodeRightQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				Update subNodeRightUpdate = new Update()
					.inc(SysDictItemMongodb.FIELD.RIGHT_NO, -moveNum)
					.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
				subNodeRightNodes.forEach(x -> {
					Query subNodeRightSubQuery = Query.query(Criteria
						.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
						.and(SysDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId()));
					UpdateResult addNodeRightUpdateResult = mongoTemplate.updateFirst(subNodeRightSubQuery, subNodeRightUpdate, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					log.info("addNodeRightUpdateResult: {}", addNodeRightUpdateResult);
				});

				Criteria newNodeCriteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.ITEM_ID).in(args.getBeforeItemId(), args.getParentItemId());
				Query newNodequery = Query.query(newNodeCriteria);

				Map<String, SysDictItemMongodb> newNodeIdMap = mongoTemplate.find(newNodequery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM).stream().collect(Collectors.toMap(SysDictItemMongodb::getItemId, x -> x));

				// 移动后的父节点
				SysDictItemMongodb newParentNode = CairoAuthConstants.ROOT_ID.equals(args.getParentItemId()) ? parentNode : newNodeIdMap.get(args.getParentItemId());
				// 移动后的左边节点
				SysDictItemMongodb newBeforeNode = beforeNode == null ? null : newNodeIdMap.get(args.getBeforeItemId());

				// 扩容
				int startAddNum = Optional.ofNullable(newBeforeNode).map(SysDictItemMongodb::getLeftNo).orElse(newParentNode.getRightNo());
				// 扩容左值
				Criteria addNodeLeftCriteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.LEFT_NO).gte(startAddNum);
				Query addNodeLeftQuery = Query.query(addNodeLeftCriteria);
				addNodeLeftQuery.with(Sort.by(Sort.Order.desc(SysDictItemMongodb.FIELD.LEFT_NO)));
				List<SysDictItemMongodb> addNodeLeftNodes = mongoTemplate.find(addNodeLeftQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				Update addNodeLeftUpdate = new Update()
					.inc(SysDictItemMongodb.FIELD.LEFT_NO, moveNum)
					.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
				addNodeLeftNodes.forEach(x -> {
					Query addNodeLeftSubQuery = Query.query(Criteria
						.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())

						.and(SysDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId()));
					UpdateResult addNodeLeftUpdateResult = mongoTemplate.updateFirst(addNodeLeftSubQuery, addNodeLeftUpdate, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					log.info("addNodeLeftUpdateResult: {}", addNodeLeftUpdateResult);
				});


				// 扩容右值
				Criteria addNodeRightCriteria = Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.RIGHT_NO).gte(startAddNum);
				Query addNodeRightQuery = Query.query(addNodeRightCriteria);
				addNodeRightQuery.with(Sort.by(Sort.Order.desc(SysDictItemMongodb.FIELD.RIGHT_NO)));
				List<SysDictItemMongodb> addNodeRightNodes = mongoTemplate.find(addNodeRightQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				Update addNodeRightUpdate = new Update()
					.inc(SysDictItemMongodb.FIELD.RIGHT_NO, moveNum)
					.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
				addNodeRightNodes.forEach(x -> {
					Query addNodeRightSubQuery = Query.query(Criteria
						.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
						.and(SysDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId()));
					UpdateResult addNodeLeftUpdateResult = mongoTemplate.updateFirst(addNodeRightSubQuery, addNodeRightUpdate, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					log.info("addNodeLeftUpdateResult: {}", addNodeLeftUpdateResult);
				});


				newNodeIdMap = mongoTemplate.find(newNodequery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM).stream().collect(Collectors.toMap(SysDictItemMongodb::getItemId, x -> x));

				// 移动后的父节点
				SysDictItemMongodb newParentNode2 = CairoAuthConstants.ROOT_ID.equals(args.getParentItemId()) ? parentNode : newNodeIdMap.get(args.getParentItemId());
				// 移动后的左边节点
				SysDictItemMongodb newBeforeNode2 = beforeNode == null ? null : newNodeIdMap.get(args.getBeforeItemId());


				int leftNo = moveNode.getLeftNo();

				moveNodes.forEach(x -> {
					if (newBeforeNode2 == null) {
						// 使用右基点
						x.setLeftNo(x.getLeftNo() - leftNo + newParentNode2.getRightNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newParentNode2.getRightNo() - moveNum);
					} else {
						// 使用左基点
						x.setLeftNo(x.getLeftNo() - leftNo + newBeforeNode2.getLeftNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newBeforeNode2.getLeftNo() - moveNum);
					}
					if (x.getItemId().equals(args.getMoveItemId())) {
						x.setParentItemId(args.getParentItemId());
					}
					x.setDepth(x.getDepth() + parentNode.getDepth() + 1 - moveNode.getDepth());

					x.getMetadata().setUpdateTime(LocalDateTime.now());
				});


				mongoTemplate.insert(moveNodes, MongodbConstants.Collection.SYS_DICT_ITEM);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("e", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("移动系统级菜单失败");
			}
		});
	}

	/**
	 * 删除系统级字典
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@Lock4j(name = "delete_sys_dict", keys = {"#appId","#args.dictId"})
	@BizLog(
		bizId = "sys_dict:delete_sys_dict",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	@SneakyThrows
	public void deleteSysDict(@Valid @NotNull String appId, @Validated DeleteSysDictArgs args) {
		Criteria sdCriteria = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(args.getDictId());
		SysDictMongodb sdm = mongoTemplate.findOne(Query.query(sdCriteria), SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
		if (sdm == null) {
			throw new ConflictBusinessException("系统级字典不存在");
		}
		// 字典
		Query sdQuery = Query.query(sdCriteria);
		Update update = Update.update(SysDictMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
		update.currentDate(SysDictMongodb.FIELD.METADATA.UPDATE_TIME);

		// 字典项
		Criteria sdiCriteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId());

		Query sdiQuery = Query.query(sdiCriteria);
		Update sdiUpdate = Update.update(SysDictItemMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
		update.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);

		SysDictMongodb insert = transactionTemplate.execute(status -> {
			try {
				mongoTemplate.updateFirst(sdQuery, update, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
				SysDictMongodb sysDictMongodb = mongoTemplate.findAndRemove(sdQuery, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);

				mongoTemplate.updateMulti(sdiQuery, sdiUpdate, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				List<SysDictItemMongodb> deleteSysDictItemMongodbList = mongoTemplate.findAllAndRemove(sdiQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				if (!deleteSysDictItemMongodbList.isEmpty()) {
					mongoTemplate.insert(deleteSysDictItemMongodbList, MongodbConstants.DeletedCollection.SYS_DICT_ITEM);
				}
				if (sysDictMongodb != null) {
					mongoTemplate.insert(sysDictMongodb, MongodbConstants.DeletedCollection.SYS_DICT);
				}
				return sysDictMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteSysDict", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除系统级字典失败");
			}
		});
		// 发送删除系统级字典完成消息
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_SYS_DICT, sdm.getAppId()),
			objectMapper.writeValueAsString(DeleteSysDictMessage.builder()
				.eventCairoUserId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.appId(sdm.getAppId())
				.dictId(args.getDictId())
				.build()
			),
			new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
		);
		//删除图标
		if (insert != null && insert.getIcon() != null) {
			publicFileCommonService.deleteFile(appId.concat("/").concat(FileKeyPrefixConstants.Collection.SYS_DICT_ICON), Collections.singletonList(insert.getIcon()));
		}
	}



	/**
	 * 删除系级集字典项
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "delete_sys_dict_item", keys = {"#appId","#args.dictId","#args.itemId"})
	@BizLog(
		bizId = "sys_dict:delete_sys_dict_item",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteSysDictItem(@Valid @NotNull String appId, @Validated DeleteSysDictItemArgs args) {
		List<String> deleteIcons = new ArrayList<>();
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query sdiQuery = Query.query(
					Criteria
						.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
						.and(SysDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId())
				);
				SysDictItemMongodb deleteNode = mongoTemplate.findOne(sdiQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				if (deleteNode == null) {
					throw new ConflictBusinessException("删除系统级字典项失败，系统级字典项不存在");
				}
				Query deleteNodeQuery = Query.query(Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.LEFT_NO).gte(deleteNode.getLeftNo())
					.and(SysDictItemMongodb.FIELD.RIGHT_NO).lte(deleteNode.getRightNo())
				);
				int inc = -(deleteNode.getRightNo() - deleteNode.getLeftNo() + 1);
				if (inc < -2) {
					throw new ConflictBusinessException("该字典项含有子字典项，请先删除子字典项后在操作");
				}

				// 更新左值
				Query otherNodeLeftQuery = Query.query(Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.LEFT_NO).gt(deleteNode.getLeftNo())
				);
				otherNodeLeftQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)));
				Update otherNodeLeftUpdate = new Update()
					.inc(SysDictItemMongodb.FIELD.LEFT_NO, inc)
					.set(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				// 更新右值
				Query otherNodeRightQuery = Query.query(Criteria
					.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(SysDictItemMongodb.FIELD.RIGHT_NO).gt(deleteNode.getRightNo())
				);
				otherNodeRightQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.RIGHT_NO)));

				Update otherNodeRightUpdate = new Update()
					.inc(SysDictItemMongodb.FIELD.RIGHT_NO, inc)
					.set(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				List<SysDictItemMongodb> deletedSysDictItemMongodbList = mongoTemplate.findAllAndRemove(deleteNodeQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				if (!deletedSysDictItemMongodbList.isEmpty()) {
					// 移动到删除影子表
					mongoTemplate.insert(deletedSysDictItemMongodbList, MongodbConstants.DeletedCollection.SYS_DICT_ITEM);
					deleteIcons.addAll(deletedSysDictItemMongodbList.stream().map(SysDictItemMongodb::getIcon).collect(Collectors.toList()));
				}

				// 移动其他菜单左值
				List<SysDictItemMongodb> otherLeftNodes = mongoTemplate.find(otherNodeLeftQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				otherLeftNodes.forEach(x -> {
					Query query = Query.query(Criteria
						.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
						.and(SysDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
					);
					UpdateResult otherNodeLeftUpdateResult = mongoTemplate.updateFirst(query, otherNodeLeftUpdate, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					log.debug("OtherNodeLeftUpdateResult: {}", otherNodeLeftUpdateResult);
				});

				// 移动其他菜单右值
				List<SysDictItemMongodb> otherRightNodes = mongoTemplate.find(otherNodeRightQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
				otherRightNodes.forEach(x -> {
					Query query = Query.query(Criteria
						.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
						.and(SysDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
					);
					UpdateResult otherNodeRightUpdateResult = mongoTemplate.updateFirst(query, otherNodeRightUpdate, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					log.debug("OtherNodeRightUpdateResult: {}", otherNodeRightUpdateResult);
				});

				// 更新ROOT节点右值
				Query rootNodeQuery = Query.query(Criteria
					.where(SysDictMongodb.FIELD.APP_ID).is(appId)
					.and(SysDictMongodb.FIELD.DICT_ID).is(args.getDictId())
				);

				Update rootNodeRightUpdate = new Update()
					.inc(SysDictMongodb.FIELD.RIGHT_NO, inc)
					.set(SysDictMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());
				UpdateResult rootRightUpdateResult = mongoTemplate.updateFirst(rootNodeQuery, rootNodeRightUpdate, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
				log.debug("RootRightUpdateResult: {}", rootRightUpdateResult);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.debug("removeSysDictItem", e);
				throw new ConflictBusinessException("删除系统级字典项失败");
			}
		});
		//删除图标
		if (!deleteIcons.isEmpty()) {
			publicFileCommonService.deleteFile(appId.concat("/").concat(FileKeyPrefixConstants.Collection.SYS_DICT_ITEM_ICON), deleteIcons);
		}
	}

	/**
	 * 同步系级级字典
	 *
	 * @param appId appId
	 */
	@NewSpan
	@Lock4j(name = "sync_sys_dict", keys = {"#appId","#args.dictId"})
	@SneakyThrows
	@BizLog(
		bizId = "sys_dict:sync_sys_dict",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void syncSysDict(String appId, @Validated SyncSysDictArgs args) {
		rabbitTemplate.convertAndSend(cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.SYNC_SYS_DICT, appId),
			objectMapper.writeValueAsString(SyncSysDictMessage.builder()
				.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.appId(appId)
				.dictId(args.getDictId())
				.build()
			),
			new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
		);
	}

	/**
	 * 根据字典复制系统级字典,字典项
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "copy_sys_dict_by_dict_id", keys = {"#appId","#args.currentDictId"})
	@BizLog(
		bizId = "sys_dict:copy_sys_dict_by_dict_id",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void copySysDictByDictId(String appId, CopySysDictByDictIdArgs args) {
		//查询新字典id是否存在
		if (mongoTemplate.exists(Query.query(Criteria.where(SysDictMongodb.FIELD.APP_ID).is(appId).and(SysDictMongodb.FIELD.DICT_ID).is(args.getNewDictId())), SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT)) {
			throw new ConflictBusinessException("字典id已存在");
		}

		//查询当前字典,字典项
		Criteria sdCriteria = Criteria
			.where(SysDictMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictMongodb.FIELD.DICT_ID).is(args.getCurrentDictId());
		Query sdQuery = Query.query(sdCriteria);
		SysDictMongodb sdMongodb = mongoTemplate.findOne(sdQuery, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
		if (sdMongodb == null) {
			throw new ConflictBusinessException("字典不存在");
		}


		Criteria sdiCriteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getCurrentDictId());
		Query sdiQuery = Query.query(sdiCriteria);
		List<SysDictItemMongodb> sdiMongodbs = mongoTemplate.find(sdiQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		transactionTemplate.executeWithoutResult(status -> {
			try {
				//添加新字典
				String icon = null;
				try {
					if (sdMongodb.getIcon() != null && !sdMongodb.getIcon().isBlank()) {
						String fileName = CoreConstants.SNOWFLAKE.nextIdStr();
						MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(sdMongodb.getIcon(),
							fileName.concat(FilesUtil.getType(sdMongodb.getIcon())));
						if (multipartFile != null) {
							List<String> urls = publicFileCommonService.uploadFile(
								appId.concat(FileKeyPrefixConstants.Collection.SYS_DICT_ICON).concat(fileName).concat(FilesUtil.getType((FilesUtil.getType(sdMongodb.getIcon())))),
								multipartFile);
							if (urls.size() > 2) icon = urls.get(2);
						}
					}
				} catch (Exception e) {
					log.info("[sd uploadPublicFile] error", e);
				}
				SysDictMongodb sdInsert = SysDictMongodb.builder()
					.dictId(args.getNewDictId())
					.appId(appId)
					.dictType(sdMongodb.getDictType())
					.dictName(sdMongodb.getDictName())
					.icon(icon)
					.isCreateItem(sdMongodb.getIsCreateItem())
					.enabled(true)
					.leftNo(sdMongodb.getLeftNo())
					.rightNo(sdMongodb.getRightNo())
					.metadata(AccountMetadataMongodb.builder()
						.createAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.updateAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.build())
					.build();

				//添加新字典项
				List<SysDictItemMongodb> sdiList = sdiMongodbs.stream().map(sdi -> {
					String sdiIcon = null;
					try {
						if (sdi.getIcon() != null && !sdi.getIcon().isBlank()) {
							String fileName = CoreConstants.SNOWFLAKE.nextIdStr();
							MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(sdi.getIcon(),
								fileName.concat(FilesUtil.getType(sdi.getIcon())));
							if (multipartFile != null) {
								List<String> urls = publicFileCommonService.uploadFile(
									appId.concat(FileKeyPrefixConstants.Collection.SYS_DICT_ICON).concat(fileName).concat(FilesUtil.getType((FilesUtil.getType(sdi.getIcon())))),
									multipartFile);
								if (urls.size() > 2) sdiIcon = urls.get(2);
							}
						}
					} catch (Exception e) {
						log.info("[sdi uploadPublicFile] error", e);
					}
					return SysDictItemMongodb.builder()
						.dictId(args.getNewDictId())
						.appId(appId)
						.itemId(sdi.getItemId())
						.itemName(sdi.getItemName())
						.icon(sdiIcon)
						.depth(sdi.getDepth())
						.parentItemId(sdi.getParentItemId())
						.leftNo(sdi.getLeftNo())
						.rightNo(sdi.getRightNo())
						.editable(sdi.getEditable())
						.enabled(sdi.getEnabled())
						.metadata(AccountMetadataMongodb.builder()
							.createAccountId(CairoSecurityContextHolder.getSubappAccountId())
							.updateAccountId(CairoSecurityContextHolder.getSubappAccountId())
							.build())
						.build();
				}).collect(Collectors.toList());

				mongoTemplate.insert(sdInsert, MongodbConstants.Collection.SYS_DICT);
				mongoTemplate.insert(sdiList, MongodbConstants.Collection.SYS_DICT_ITEM);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.info("copySysDictByDictId", e);
				throw new ConflictBusinessException("复制字典失败");
			}
		});
	}


	private List<MetadataSysDict> getSysDictList(String appId, List<SysDictMongodb> mongodbList) {
		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(mongodbList.stream().map(SysDictMongodb::getMetadata).collect(Collectors.toList()));

		Map<String, Account> accountMap = accountClientApiService.getAccountList(GetAccountListArgs.builder().accountIds(metadataAccountIds).build()).stream()
			.collect(Collectors.toMap(Account::getAccountId, g -> g));;

		return mongodbList.stream()
			.map(x -> SysDictConverter.convertMetadataSysDict(x, accountMap))
			.collect(Collectors.toList());
	}

	private List<SysDict> getSysDictList(List<SysDictMongodb> mongodbList, Map<String, List<SysDictItemMongodb>> itemMap) {
		return mongodbList.stream()
			.map(x -> SysDictConverter.convertSysDict(x, itemMap.get(x.getDictId())))
			.collect(Collectors.toList());
	}

}
