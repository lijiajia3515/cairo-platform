package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.dict.biz;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import io.github.lijiajia3515.cairo.auth.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.DeleteBizDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictItemPageInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.ModifyBizDictItemIconArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.ModifyBizDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.ModifyBizDictItemStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.PutBizDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.RestoreBizDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.MetadataBizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.MetadataBizDictItem;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictMongodb;
import io.github.lijiajia3515.cairo.auth.modules.dict.biz.*;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileCommonService;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;

/**
 * [tenant_subapp_user/api]tenant app subapp biz dict service
 */
@Slf4j
@Validated
@Component
public class BizDictTenantSubappApiService {
	private final MongoTemplate readMongoTemplate;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final BizDictCommonService bizDictCommonService;
	private final PublicFileCommonService publicFileCommonService;


	BizDictTenantSubappApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												TenantAppUserCommonService tenantAppUserCommonService,
												@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
												TransactionTemplate transactionTemplate,
												BizDictCommonService bizDictCommonService,
												PublicFileCommonService publicFileCommonService) {
		this.tenantAppUserCommonService = tenantAppUserCommonService;
		this.readMongoTemplate = readMongoTemplate;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.bizDictCommonService = bizDictCommonService;
		this.publicFileCommonService = publicFileCommonService;
	}

	/**
	 * 恢复应用级字典
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "restore", keys = {"#args.dictId"})
	@BizLog(
		bizId = "biz_dict:restore",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void restoreBizDict(String tenantId, String appId, RestoreBizDictArgs args) {

		// 应用字典
		Criteria bdCriteria = Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictMongodb.FIELD.DICT_ID).is(args.getDictId());
		Query bdQuery = Query.query(bdCriteria);
		BizDictMongodb bdMongodb = mongoTemplate.findOne(bdQuery, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
		if (bdMongodb == null)
			throw new ConflictBusinessException("恢复业务级字典失败,字典不存在");
		Criteria criteria = Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(bdMongodb.getAppId())
			.and(BizDictMongodb.FIELD.TENANT_ID).is(bdMongodb.getTenantId())
			.and(BizDictMongodb.FIELD.DICT_ID).is(bdMongodb.getDictId());
		Query query = Query.query(criteria);
		Update update = Update.update(BizDictMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId())
			.currentDate(BizDictMongodb.FIELD.METADATA.UPDATE_TIME)
			.set(BizDictMongodb.FIELD.DICT_NAME, bdMongodb.getReductionDictName())
			.set(BizDictMongodb.FIELD.ICON, bdMongodb.getReductionIcon())
			.set(BizDictMongodb.FIELD.REDUCTION_VERSION, bdMongodb.getSyncVersion());
		transactionTemplate.executeWithoutResult(status -> mongoTemplate.updateFirst(query, update, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT));
		// 应用字典项
		Criteria bdiCriteria = Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId());
		Query bziQuery = Query.query(bdiCriteria);
		List<BizDictItemMongodb> bdiMongodbs = mongoTemplate.find(bziQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		bdiMongodbs.forEach(bdi -> {
			Query updateBdiQuery = Query.query(Criteria
				.where(BizDictItemMongodb.FIELD.APP_ID).is(bdi.getAppId())
				.and(BizDictItemMongodb.FIELD.TENANT_ID).is(bdi.getTenantId())
				.and(BizDictItemMongodb.FIELD.ITEM_ID).is(bdi.getItemId()));
			Update bdiUpdate = Update.update(BizDictItemMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId()).currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME)
				.set(BizDictItemMongodb.FIELD.ITEM_NAME, bdi.getReductionItemName())
				.set(BizDictItemMongodb.FIELD.ICON, bdi.getReductionIcon())
				.set(BizDictItemMongodb.FIELD.REMARK, bdi.getReductionRemark())
				.set(BizDictItemMongodb.FIELD.REDUCTION_VERSION, bdi.getSyncVersion());
			transactionTemplate.executeWithoutResult(status -> mongoTemplate.updateFirst(updateBdiQuery, bdiUpdate, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM));
		});

	}


	/**
	 * 查询应用级字典项信息
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "biz_dict:get_biz_dict_item_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataBizDictItem> getBizDictItemInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetBizDictItemInfoArgs args) {
		Criteria sdiCriteria = Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(BizDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId());
		Query sdiQuery = Query.query(sdiCriteria);
		BizDictItemMongodb itemMongodb = readMongoTemplate.findOne(sdiQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

		if (itemMongodb == null) return Collections.emptyList();

		Criteria sdiParentCriteria = Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(BizDictItemMongodb.FIELD.LEFT_NO).lt(itemMongodb.getLeftNo())
			.and(BizDictItemMongodb.FIELD.RIGHT_NO).gt(itemMongodb.getRightNo());
		Query sdiParentQuery = Query.query(sdiParentCriteria);
		sdiParentQuery.with(Sort.by(Sort.Order.asc(BizDictItemMongodb.FIELD.DEPTH)));

		List<BizDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiParentQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		itemMongodbList.add(itemMongodb);

		Map<String, TenantAppUser> metadataUserMap = tenantAppUserCommonService.getUserMapByUserIds(
			tenantId,
			appId,
			CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(itemMongodbList.stream().map(BizDictItemMongodb::getMetadata).collect(Collectors.toList()))
		);

		return itemMongodbList.stream()
			.map(x -> BizDictConverter.convertMetadataBizDictItem(x, metadataUserMap))
			.collect(Collectors.toList());
	}

	/**
	 * 查询应用级字典项分页信息
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "biz_dict:get_biz_dict_item_page_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataBizDictItem> getBizDictItemPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, GetBizDictItemPageInfoArgs args) {
		Criteria criteria = Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(BizDictItemMongodb.FIELD.PARENT_ITEM_ID).is(args.getParentItemId());

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.by(BizDictItemMongodb.FIELD.LEFT_NO)));
		long total = readMongoTemplate.count(query, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		query.with(args.pageable());
		List<BizDictItemMongodb> dictItemMongodbs = readMongoTemplate.find(query, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		Map<String, TenantAppUser> metadataUserMap = tenantAppUserCommonService.getUserMapByUserIds(
			tenantId,
			appId,
			CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(dictItemMongodbs.stream().map(BizDictItemMongodb::getMetadata).collect(Collectors.toList()))
		);
		List<MetadataBizDictItem> dictItems = dictItemMongodbs.stream()
			.map(x -> BizDictConverter.convertMetadataBizDictItem(x, metadataUserMap))
			.collect(Collectors.toList());
		return new Page<>(args, dictItems, total);
	}


	/**
	 * 查询业务级字典列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "biz_dict:get_biz_dict_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataBizDict> getBizDictList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetBizDictArgs args) {
		Criteria sdc = Criteria.where(BizDictMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId);
		if (args.getKeyword() != null) {
			sdc.and(BizDictMongodb.FIELD.DICT_NAME).regex(args.getKeyword());
		}
		if (args.getDictIds() != null && !args.getDictIds().isEmpty()) {
			sdc.and(BizDictMongodb.FIELD.DICT_ID).in(args.getDictIds());
		}

		Query query = Query.query(sdc);
		query.with(Sort.by(Sort.Order.desc(BizDictMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<BizDictMongodb> bizDictMongodbList = readMongoTemplate.find(query, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
		return getMetadataBizDictList(tenantId, appId, bizDictMongodbList);
	}

	/**
	 * 查询业务级字典分页列表
	 *
	 * @param appId appId
	 * @param args  query args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "biz_dict:get_biz_dict_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataBizDict> getBizDictPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetBizDictArgs args) {
		Criteria sdc = Criteria.where(BizDictMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId);
		if (args.getKeyword() != null) {
			sdc.and(BizDictMongodb.FIELD.DICT_NAME).regex(args.getKeyword());
		}
		if (args.getDictIds() != null && !args.getDictIds().isEmpty()) {
			sdc.and(BizDictMongodb.FIELD.DICT_ID).in(args.getDictIds());
		}

		Query query = Query.query(sdc);
		long total = readMongoTemplate.count(query, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizDictMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<BizDictMongodb> bizDictMongodbList = readMongoTemplate.find(query, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
		List<MetadataBizDict> contents = getMetadataBizDictList(tenantId, appId, bizDictMongodbList);
		return new Page<>(args, contents, total);
	}

	/**
	 * 查询业务级字典列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "biz_dict:get_biz_dict_detail_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<BizDict> getBizDictDetailList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetBizDictDetailArgs args) {
		Criteria sdc = Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictMongodb.FIELD.DICT_ID).in(args.getDictIds());

		Query query = Query.query(sdc);
		query.with(Sort.by(Sort.Order.desc(BizDictMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<BizDictMongodb> bizDictMongodbList = readMongoTemplate.find(query, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
		if (bizDictMongodbList.isEmpty()) return Collections.emptyList();
		List<String> dictIds = bizDictMongodbList.stream().map(BizDictMongodb::getDictId).collect(Collectors.toList());

		Criteria sdic = Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).in(dictIds);

		Query sdiq = Query.query(sdic);
		sdiq.with(Sort.by(
			Sort.Order.asc(BizDictItemMongodb.FIELD.DICT_ID),
			Sort.Order.asc(BizDictItemMongodb.FIELD.DEPTH),
			Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)
		));

		List<BizDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiq, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		Map<String, List<BizDictItemMongodb>> dictItemMongodbListMap = itemMongodbList.stream().collect(Collectors.groupingBy(BizDictItemMongodb::getDictId));

		return getBizDictList(bizDictMongodbList, dictItemMongodbListMap);
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
		bizId = "biz_dict:get_biz_dict_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "dictId", value = "#dictId"),
		}
	)
	public MetadataBizDict getBizDictInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @NotNull String dictId) {
		Criteria sdc = Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictMongodb.FIELD.DICT_ID).is(dictId);
		Query sdq = Query.query(sdc);

		BizDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);

		if (dictMongodb == null) return null;
		Map<String, TenantAppUser> metadataUserMap = tenantAppUserCommonService.getUserMapByUserIds(
			tenantId,
			appId,
			CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(Stream.of(dictMongodb.getMetadata()).collect(Collectors.toList()))
		);
		return BizDictConverter.convertMetadataBizDict(dictMongodb, metadataUserMap);
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
	public MetadataBizDict getBizDictDetailInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @NotNull String dictId) {
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
		List<TenantAppUserMetadataMongodb> metadataUserMongodbList = Stream.concat(
			Optional.ofNullable(dictMongodb.getMetadata()).stream(),
			itemMongodbList.stream().map(BizDictItemMongodb::getMetadata)
		).collect(Collectors.toList());
		Map<String, TenantAppUser> metadataUserMap = tenantAppUserCommonService.getUserMapByUserIds(
			tenantId,
			appId,
			CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(metadataUserMongodbList)
		);
		MetadataBizDict dict = BizDictConverter.convertMetadataBizDict(dictMongodb, metadataUserMap);
		List<MetadataBizDictItem> items = itemMongodbList.stream()
			.map(x -> BizDictConverter.convertMetadataBizDictItem(x, metadataUserMap))
			.collect(Collectors.toList());

		List<MetadataBizDictItem> subItems = Tree2Converter.build(items, ROOT_ID);
		dict.setItems(subItems);
		return dict;
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
		bizId = "biz_dict:get_biz_dict_sub_item_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataBizDictItem> getBizDictSubItemList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetBizDictSubItemArgs args) {
		Criteria sdc = Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictMongodb.FIELD.DICT_ID).is(args.getDictId());
		Query sdq = Query.query(sdc);

		BizDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
		if (dictMongodb == null) return null;

		Criteria sdic = Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(BizDictItemMongodb.FIELD.PARENT_ITEM_ID).is(args.getParentItemId());
		Query sdiq = Query.query(sdic);
		sdiq.with(Sort.by(
			Sort.Order.asc(BizDictItemMongodb.FIELD.DEPTH),
			Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)
		));

		List<BizDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiq, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

		List<TenantAppUserMetadataMongodb> metadataUserMongodbList = Stream.concat(
			Optional.ofNullable(dictMongodb.getMetadata()).stream(),
			itemMongodbList.stream().map(BizDictItemMongodb::getMetadata)
		).collect(Collectors.toList());
		Map<String, TenantAppUser> metadataUserMap = tenantAppUserCommonService.getUserMapByUserIds(
			tenantId,
			appId,
			CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(metadataUserMongodbList)
		);

		return itemMongodbList.stream()
			.map(x -> BizDictConverter.convertMetadataBizDictItem(x, metadataUserMap))
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
		bizId = "biz_dict:get_biz_dict_sub_item_tree_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataBizDictItem> getBizDictSubItemTreeList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetBizDictSubItemArgs args) {
		String parentItemId = Optional.ofNullable(args.getParentItemId()).orElse(ROOT_ID);
		Criteria sdc = Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictMongodb.FIELD.DICT_ID).is(args.getDictId());
		Query sdq = Query.query(sdc);

		BizDictMongodb dictMongodb = readMongoTemplate.findOne(sdq, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
		if (dictMongodb == null) return Collections.emptyList();
		int leftNo, rightNo;
		if (!ROOT_ID.equals(parentItemId)) {
			Criteria sdic = Criteria
				.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
				.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
				.and(BizDictItemMongodb.FIELD.PARENT_ITEM_ID).is(args.getParentItemId());
			Query sdiq = Query.query(sdic);

			BizDictItemMongodb parentItemMongodb = readMongoTemplate.findOne(sdiq, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
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
			.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(BizDictItemMongodb.FIELD.LEFT_NO).gte(leftNo)
			.and(BizDictItemMongodb.FIELD.RIGHT_NO).lte(rightNo);
		Query sdiq = Query.query(sdic);
		sdiq.with(Sort.by(
			Sort.Order.asc(BizDictItemMongodb.FIELD.DEPTH),
			Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)
		));

		List<BizDictItemMongodb> itemMongodbList = readMongoTemplate.find(sdiq, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

		List<TenantAppUserMetadataMongodb> metadataUserMongodbList = Stream.concat(
			Optional.ofNullable(dictMongodb.getMetadata()).stream(),
			itemMongodbList.stream().map(BizDictItemMongodb::getMetadata)
		).collect(Collectors.toList());
		Map<String, TenantAppUser> metadataUserMap = tenantAppUserCommonService.getUserMapByUserIds(
			tenantId,
			appId,
			CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(metadataUserMongodbList)
		);

		List<MetadataBizDictItem> subItems = itemMongodbList.stream()
			.map(x -> BizDictConverter.convertMetadataBizDictItem(x, metadataUserMap))
			.collect(Collectors.toList());

		return Tree2Converter.build(subItems, args.getParentItemId());
	}

	/**
	 * 添加业务级字典项
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "biz_dict:put_biz_dict_item",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void putBizDictItem(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated PutBizDictItemArgs args) {
		String dictId = args.getDictId();
		Query dictQuery = Query.query(Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(appId)
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictMongodb.FIELD.DICT_ID).is(dictId)
		);
		BizDictMongodb dict = mongoTemplate.findOne(dictQuery, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
		if (dict == null)
			throw new ConflictBusinessException("添加业务级字典项失败,字典不存在");
		if (dict.getIsCreateItem() == null || !dict.getIsCreateItem())
			throw new ConflictBusinessException("添加业务级字典项失败,不允许添加");
		transactionTemplate.executeWithoutResult(status -> {
			try {
				String parentItemId = Optional.ofNullable(args.getParentItemId()).orElse(ROOT_ID);
				Query parentQuery = Query.query(Criteria
					.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictItemMongodb.FIELD.DICT_ID).is(dictId)
					.and(BizDictItemMongodb.FIELD.ITEM_ID).is(parentItemId)
				);
				BizDictItemMongodb parentItem = mongoTemplate.findOne(parentQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				if (parentItem == null && ROOT_ID.equals(parentItemId)) {
					parentItem = bizDictCommonService.getRootItem(appId, tenantId, dictId);
				}

				if (parentItem == null) {
					throw new ConflictBusinessException("业务级字典项ParentItemId错误");
				}

				// find brother
				Query brotherNodeQuery = Query.query(
					Criteria.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(BizDictItemMongodb.FIELD.DICT_ID).is(dictId)
						.and(BizDictItemMongodb.FIELD.PARENT_ITEM_ID).is(parentItemId)
				);
				brotherNodeQuery.with(Sort.by(Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)));

				List<BizDictItemMongodb> brotherNodes = mongoTemplate.find(brotherNodeQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				Optional<BizDictItemMongodb> afterNode = brotherNodes.stream().filter(a -> a.getItemId().equals(args.getBeforeItemId())).findFirst();
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
					.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictItemMongodb.FIELD.DICT_ID).is(dictId)
					.and(BizDictItemMongodb.FIELD.LEFT_NO).gte(position)
				);
				leftParentQuery.with(Sort.by(Sort.Order.desc(BizDictItemMongodb.FIELD.LEFT_NO)));
				List<BizDictItemMongodb> leftNodes = mongoTemplate.find(leftParentQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				leftNodes.forEach(x -> {
					Query query = Query.query(Criteria
						.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(BizDictItemMongodb.FIELD.DICT_ID).is(dictId)
						.and(BizDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
					);
					Update update = new Update()
						.inc(BizDictItemMongodb.FIELD.LEFT_NO, 2)
						.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult updateResult = mongoTemplate.updateFirst(query, update, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
					log.debug("update biz dict item left values to db : [{}]", updateResult);
				});

				// 右值扩容
				Query rightParentQuery = Query.query(Criteria
					.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictItemMongodb.FIELD.DICT_ID).is(dictId)
					.and(BizDictItemMongodb.FIELD.RIGHT_NO).gte(position)
				);
				rightParentQuery.with(Sort.by(Sort.Order.desc(BizDictItemMongodb.FIELD.RIGHT_NO)));
				List<BizDictItemMongodb> moveRightNodes = mongoTemplate.find(rightParentQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				moveRightNodes.forEach(x -> {
					Query query = Query.query(Criteria
						.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(BizDictItemMongodb.FIELD.DICT_ID).is(dictId)
						.and(BizDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
					);
					Update update = new Update()
						.inc(BizDictItemMongodb.FIELD.RIGHT_NO, 2)
						.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult updateResult = mongoTemplate.updateFirst(query, update, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
					log.debug("update biz dict item right values to db : [{}]", updateResult);
					// TODO 更新字典表 left,right
				});

				// 更新root右值
				Query bizDictQuery = Query.query(Criteria
					.where(BizDictMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictMongodb.FIELD.DICT_ID).is(dictId)
				);
				Update bizDictUpdate = new Update()
					.inc(BizDictMongodb.FIELD.RIGHT_NO, 2)
					.currentDate(BizDictMongodb.FIELD.METADATA.UPDATE_TIME);
				bizDictUpdate.inc(BizDictMongodb.FIELD.REDUCTION_VERSION);
				UpdateResult updateResult = mongoTemplate.updateFirst(bizDictQuery, bizDictUpdate, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
				log.debug("update biz dict right values to db : [{}]", updateResult);

				// 插入字典项
				BizDictItemMongodb itemMongodb = BizDictItemMongodb.builder()
					.appId(appId)
					.dictId(dictId)
					.parentItemId(parentItemId)
					.itemId(args.getItemId())
					.itemName(args.getItemName())
					.enabled(Optional.ofNullable(args.getEnabled()).orElse(true))
					.remark(args.getRemark())
					.editable(args.getEditable())
					.leftNo(left)
					.rightNo(right)
					.tenantId(tenantId)
					.depth(parentItem.getDepth() + 1)
					.reductionItemName(args.getItemName())
					.reductionRemark(args.getRemark())
					.reductionVersion(1)
					.isSync(false)
					.isSyncIcon(false)
					.metadata(TenantAppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.updateUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.build())
					.build();
				BizDictItemMongodb insert = mongoTemplate.insert(itemMongodb, MongodbConstants.Collection.BIZ_DICT_ITEM);
				log.info("insert biz dict item to db : {}", insert.getItemId());
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.info("putBizDictItem", e);
				throw new ConflictBusinessException("添加业务级字典项失败");
			}
		});

	}

	/**
	 * 修改业务级字典项
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_biz_dict_item_info", keys = {"#tenantId","#appId","#args.dictId","#args.itemId"})
	@BizLog(
		bizId = "dict:modify_biz_dict_item_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyBizDictItemInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyBizDictItemInfoArgs args) {

		UpdateResult result = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(BizDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId());
				Query query = Query.query(criteria);
				BizDictItemMongodb node = mongoTemplate.findOne(query, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

				if (node == null) {
					throw new ConflictBusinessException("更新业务级字典项失败，业务级字典项不存在");
				}
				if (node.getEditable() == null || !node.getEditable())
					throw new ConflictBusinessException("更新业务级字典项失败，不允许编辑");
				Update update = new Update();
				if (args.getItemName() != null) {
					update.set(BizDictItemMongodb.FIELD.ITEM_NAME, args.getItemName());
					update.set(BizDictItemMongodb.FIELD.REDUCTION_ITEM_NAME, args.getItemName());
				}


				if (args.getRemark() != null) {
					update.set(BizDictItemMongodb.FIELD.REMARK, args.getRemark());
					update.set(BizDictItemMongodb.FIELD.REDUCTION_REMARK, args.getRemark());
				}

				if (args.getIcon() != null) {
					update.set(BizDictItemMongodb.FIELD.ICON, args.getIcon());
					update.set(BizDictItemMongodb.FIELD.IS_SYNC_ICON, false);
					update.set(BizDictItemMongodb.FIELD.REDUCTION_ICON, args.getIcon());
				}

				update.set(BizDictItemMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());

				update.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
				update.inc(BizDictItemMongodb.FIELD.REDUCTION_VERSION);

				UpdateResult bdiResult = mongoTemplate.updateFirst(query, update, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				// 修改业务字典版本号
				Query bizDictQuery = Query.query(Criteria
					.where(BizDictMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictMongodb.FIELD.DICT_ID).is(args.getDictId())
				);
				Update bizDictUpdate = new Update()
					.inc(BizDictMongodb.FIELD.REDUCTION_VERSION, 1);
				bizDictUpdate.set(BizDictMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				bizDictUpdate.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(bizDictQuery, bizDictUpdate, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
				return bdiResult;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyBizDictItemInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改业务级字典项失败");
			}
		});

		if (result == null || result.getModifiedCount() < 1) {
			throw new ConflictBusinessException("修改业务级字典项失败");
		}
	}


	/**
	 * 修改业务级字典项图标
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_biz_dict_item_icon",  keys = {"#tenantId","#appId","#args.dictId","#args.itemId"})
	@BizLog(
		bizId = "dict:modify_biz_dict_item_icon",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyBizDictItemIcon(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyBizDictItemIconArgs args) {

		UpdateResult result = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(BizDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId());
				Query query = Query.query(criteria);
				BizDictItemMongodb node = mongoTemplate.findOne(query, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

				if (node == null) {
					throw new ConflictBusinessException("更新业务级字典项失败，业务级字典项不存在");
				}
				if (node.getEditable() == null || !node.getEditable())
					throw new ConflictBusinessException("更新业务级字典项失败，不允许编辑");
				Update update = new Update();

				if (args.getIcon() != null&& !args.getIcon().isBlank()) {
					String bdiIcon = null;
						String fileName = CoreConstants.nextIdStr();
						MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(args.getIcon(),
							fileName.concat(FilesUtil.getType(args.getIcon())));
						if (multipartFile != null) {
							List<String> urls = publicFileCommonService.uploadFile(tenantId
									.concat("/")
									.concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ITEM_ICON)
									.concat("/")
									.concat(fileName)
									.concat(FilesUtil.getType((FilesUtil.getType(args.getIcon())))),
								multipartFile);
							if (urls.size() > 2) bdiIcon = urls.get(2);
						}

					update.set(BizDictItemMongodb.FIELD.ICON, bdiIcon);
					update.set(BizDictItemMongodb.FIELD.IS_SYNC_ICON, false);
					update.set(BizDictItemMongodb.FIELD.REDUCTION_ICON, bdiIcon);
				} else {
					update.set(BizDictItemMongodb.FIELD.ICON, null);
				}
				update.set(BizDictItemMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());

				update.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
				update.inc(BizDictItemMongodb.FIELD.REDUCTION_VERSION);

				UpdateResult bdiResult = mongoTemplate.updateFirst(query, update, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				// 修改业务字典版本号
				Query bizDictQuery = Query.query(Criteria
					.where(BizDictMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictMongodb.FIELD.DICT_ID).is(args.getDictId())
				);
				Update bizDictUpdate = new Update()
					.inc(BizDictMongodb.FIELD.REDUCTION_VERSION, 1);
				bizDictUpdate.set(BizDictMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				bizDictUpdate.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(bizDictQuery, bizDictUpdate, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
				return bdiResult;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyBizDictItemIcon", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改业务级字典项图标失败");
			}
		});

		if (result == null || result.getModifiedCount() < 1) {
			throw new ConflictBusinessException("修改业务级字典项图标失败");
		}
	}

	/**
	 * 修改业务级字典状态
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_biz_dict_item_status",  keys = {"#tenantId","#appId","#args.dictId","#args.itemId"})
	@BizLog(
		bizId = "dict:modify_biz_dict_item_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyBizDictItemStatus(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyBizDictItemStatusArgs args) {
		UpdateResult result = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(BizDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId());
				Query query = Query.query(criteria);
				BizDictItemMongodb node = mongoTemplate.findOne(query, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

				if (node == null) {
					throw new ConflictBusinessException("更新业务级字典项状态失败，业务级字典项不存在");
				}
				if (node.getEditable() == null || !node.getEditable())
					throw new ConflictBusinessException("更新业务级字典项状态失败，不允许编辑");
				Update update = new Update();
				if (args.getEnabled() != null) {
					update.set(BizDictItemMongodb.FIELD.ENABLED, args.getEnabled());
				}

				update.set(BizDictItemMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());

				update.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
				update.inc(BizDictItemMongodb.FIELD.REDUCTION_VERSION);

				UpdateResult updateFirst = mongoTemplate.updateFirst(query, update, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);

				// 修改业务字典版本号
				Query bizDictQuery = Query.query(Criteria
					.where(BizDictMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictMongodb.FIELD.DICT_ID).is(args.getDictId())
				);
				Update bizDictUpdate = new Update()
					.inc(BizDictMongodb.FIELD.REDUCTION_VERSION, 1);
				mongoTemplate.updateFirst(bizDictQuery, bizDictUpdate, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
				return updateFirst;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyBizDictItemStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改业务级字典项状态失败");
			}
		});

		if (result == null || result.getModifiedCount() < 1) {
			throw new ConflictBusinessException("修改业务级字典项状态失败");
		}
	}

	/**
	 * 删除业务级字典项
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "delete_biz_dict_item", keys = {"#tenantId","#appId","#args.dictId","#args.itemId"})
	@BizLog(
		bizId = "biz_dict:delete_biz_dict_item",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteBizDictItem(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated DeleteBizDictItemArgs args) {
		List<String> deleteIcons = new ArrayList<>();
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query sdiQuery = Query.query(
					Criteria
						.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
						.and(BizDictItemMongodb.FIELD.ITEM_ID).is(args.getItemId())
				);
				BizDictItemMongodb deleteNode = mongoTemplate.findOne(sdiQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				if (deleteNode == null) {
					throw new ConflictBusinessException("删除业务级字典项失败，业务级字典项不存在");
				}
				if (deleteNode.getEditable() == null || !deleteNode.getEditable())
					throw new ConflictBusinessException("删除业务级字典项失败，不允许编辑");
				Query deleteNodeQuery = Query.query(Criteria
					.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(BizDictItemMongodb.FIELD.LEFT_NO).gte(deleteNode.getLeftNo())
					.and(BizDictItemMongodb.FIELD.RIGHT_NO).lte(deleteNode.getRightNo())
				);
				int inc = -(deleteNode.getRightNo() - deleteNode.getLeftNo() + 1);
				if (inc < -2) {
					throw new ConflictBusinessException("该字典项含有子字典项，请先删除子字典项后在操作");
				}

				// 更新左值
				Query otherNodeLeftQuery = Query.query(Criteria
					.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(BizDictItemMongodb.FIELD.LEFT_NO).gt(deleteNode.getLeftNo())
				);
				otherNodeLeftQuery.with(Sort.by(Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)));
				Update otherNodeLeftUpdate = new Update()
					.inc(BizDictItemMongodb.FIELD.LEFT_NO, inc)
					.set(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				// 更新右值
				Query otherNodeRightQuery = Query.query(Criteria
					.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
					.and(BizDictItemMongodb.FIELD.RIGHT_NO).gt(deleteNode.getRightNo())
				);
				otherNodeRightQuery.with(Sort.by(Sort.Order.asc(BizDictItemMongodb.FIELD.RIGHT_NO)));

				Update otherNodeRightUpdate = new Update()
					.inc(BizDictItemMongodb.FIELD.RIGHT_NO, inc)
					.set(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				List<BizDictItemMongodb> deletedBizDictItemMongodbList = mongoTemplate.findAllAndRemove(deleteNodeQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				if (!deletedBizDictItemMongodbList.isEmpty()) {
					// 移动到删除影子表
					mongoTemplate.insert(deletedBizDictItemMongodbList, MongodbConstants.DeletedCollection.BIZ_DICT_ITEM);
					deleteIcons.addAll(deletedBizDictItemMongodbList.stream().filter(x -> !x.getIsSyncIcon()).map(BizDictItemMongodb::getIcon).collect(Collectors.toList()));
				}

				// 移动其他菜单左值
				List<BizDictItemMongodb> otherLeftNodes = mongoTemplate.find(otherNodeLeftQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				otherLeftNodes.forEach(x -> {
					Query query = Query.query(Criteria
						.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
						.and(BizDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
					);
					UpdateResult otherNodeLeftUpdateResult = mongoTemplate.updateFirst(query, otherNodeLeftUpdate, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
					log.debug("OtherNodeLeftUpdateResult: {}", otherNodeLeftUpdateResult);
				});

				// 移动其他菜单右值
				List<BizDictItemMongodb> otherRightNodes = mongoTemplate.find(otherNodeRightQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
				otherRightNodes.forEach(x -> {
					Query query = Query.query(Criteria
						.where(BizDictItemMongodb.FIELD.APP_ID).is(appId)
						.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(BizDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
						.and(BizDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
					);
					UpdateResult otherNodeRightUpdateResult = mongoTemplate.updateFirst(query, otherNodeRightUpdate, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
					log.debug("OtherNodeRightUpdateResult: {}", otherNodeRightUpdateResult);
				});

				// 更新ROOT节点右值
				Query rootNodeQuery = Query.query(Criteria
					.where(BizDictMongodb.FIELD.APP_ID).is(appId)
					.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(BizDictMongodb.FIELD.DICT_ID).is(args.getDictId())
				);

				Update rootNodeRightUpdate = new Update()
					.inc(BizDictMongodb.FIELD.RIGHT_NO, inc)
					.set(BizDictMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());
				UpdateResult rootRightUpdateResult = mongoTemplate.updateFirst(rootNodeQuery, rootNodeRightUpdate, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
				log.debug("RootRightUpdateResult: {}", rootRightUpdateResult);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.debug("removeBizDictItem", e);
				throw new ConflictBusinessException("删除业务级字典项失败");
			}
		});
		//删除业务级字典项图标
		if (!deleteIcons.isEmpty()) {
			publicFileCommonService.deleteFile(appId.concat("/").concat(FileKeyPrefixConstants.Collection.SYS_DICT_ITEM_ICON), deleteIcons);
		}
	}


	private List<MetadataBizDict> getMetadataBizDictList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, List<BizDictMongodb> mongodbList) {
		Map<String, TenantAppUser> metadataUserMap = tenantAppUserCommonService.getUserMapByUserIds(
			tenantId,
			appId,
			CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(mongodbList.stream().map(BizDictMongodb::getMetadata).collect(Collectors.toList()))
		);

		return mongodbList.stream()
			.map(x -> BizDictConverter.convertMetadataBizDict(x, metadataUserMap))
			.collect(Collectors.toList());
	}

	private List<BizDict> getBizDictList(List<BizDictMongodb> mongodbList, Map<String, List<BizDictItemMongodb>> sortedBizItemMongodbListMap) {

		return mongodbList.stream()
			.map(x -> BizDictConverter.convertBizDict(x, sortedBizItemMongodbListMap.get(x.getDictId())))
			.collect(Collectors.toList());
	}

}
