package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.wxmp.template_msg;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.api.client.app.AppClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.CreateWxmpTemplateMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.DeleteWxmpTemplateMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.GetWxmpTemplateMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.ModifyWxmpTemplateMsgInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.ModifyWxmpTemplateMsgStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgArgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.MetadataWxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg.WxmpTemplateMsgConverter;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

/**
 * [cairo_web_manage/api] wxmp template msg service
 */
@Slf4j
@Validated
@Component
public class WxmpTemplateMsgCairoWebManageApiService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final AppClientApiService appClientApiService;

	WxmpTemplateMsgCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
												TransactionTemplate transactionTemplate,
												@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												AppUserCommonService appUserCommonService,
												CairoSecurityProperties cairoSecurityProperties,
												AppClientApiService appClientApiService) {
		this.appUserCommonService = appUserCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.appClientApiService = appClientApiService;
	}
	/**
	 * 创建微信模板
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_template_msg:create_wxmp_template_msg",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createWxmpTemplateMsg(@Valid @NotNull String appId, @Validated CreateWxmpTemplateMsgArgs args) {
		String currentUserId = CairoSecurityContextHolder.getAppUserId();
		WxmpTemplateMsgMongodb wxmsTemplateMongodb = WxmpTemplateMsgMongodb.builder()
			.appId(appId)
			.bizId(args.getBizId())
			.wxmpProviderId(args.getWxmpProviderId())
			.templateName(args.getTemplateName())
			.templateCode(args.getTemplateCode())
			.templateType(args.getTemplateType())
			.templateText(args.getTemplateText())
			.enabled(false)
			.jumpUrl(args.getJumpUrl())
			.metadata(AppUserMetadataMongodb.builder()
				.createUserId(currentUserId)
				.updateUserId(currentUserId)
				.build()
			)
			.build();
		AtomicInteger sort = new AtomicInteger(0);
		List<WxmpTemplateMsgArgMongodb> wxmsTemplateArgMongodbList = Optional.ofNullable(args.getArgs()).orElse(Collections.emptyList())
			.stream().map(x -> WxmpTemplateMsgArgMongodb.builder()
				.appId(appId)
				.bizId(args.getBizId())
				.argName(x.getArgName())
				.argType(x.getArgType())
				.argCode(x.getArgCode())
				.templateArgCode(x.getTemplateArgCode())
				.defaultColor(x.getDefaultColor())
				.metadata(AppUserMetadataMongodb.builder()
					.createUserId(currentUserId)
					.updateUserId(currentUserId)
					.build()
				)
				.sort(sort.incrementAndGet())
				.build()
			).collect(Collectors.toList());

		transactionTemplate.executeWithoutResult(status -> {
			try {
				mongoTemplate.insert(wxmsTemplateMongodb, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);
				mongoTemplate.insert(wxmsTemplateArgMongodbList, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);
			} catch (Exception e) {
				log.debug("createWxmpTemplateMsg", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建微信模板异常");
			}
		});
	}

	/**
	 * 修改微信模板信息
	 *
	 * @param appId 应用ID
	 * @param args  参数
	 */
	@Lock4j(name = "modify_wxmp_template_msg_info", keys = {"#appId","#args.bizId"})
	@BizLog(
		bizId = "wxmp_template_msg:modify_wxmp_template_msg_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	public void modifyWxmpTemplateMsgInfo(@Valid @NotNull String appId, @Validated ModifyWxmpTemplateMsgInfoArgs args) {
		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Criteria existsCriteria = Criteria
					.where(WxmpTemplateMsgMongodb.FIELD.APP_ID).is(appId)
					.and(WxmpTemplateMsgMongodb.FIELD.BIZ_ID).is(args.getBizId())
					.and(WxmpTemplateMsgMongodb.FIELD.ENABLED).is(false);
				Query existsQuery = Query.query(existsCriteria);
				boolean exists = mongoTemplate.exists(existsQuery, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);
				if (!exists) {
					throw new ConflictBusinessException("请禁用后在进行编辑操作");
				}
				Criteria updateCriteria = Criteria
					.where(WxmpTemplateMsgMongodb.FIELD.APP_ID).is(appId)
					.and(WxmpTemplateMsgMongodb.FIELD.BIZ_ID).is(args.getBizId());
				Query updateQuery = Query.query(updateCriteria);
				Update update = new Update();

				if (args.getWxmpProviderId() != null) {
					update.set(WxmpTemplateMsgMongodb.FIELD.WXMP_PROVIDER_ID, args.getWxmpProviderId());
				}

				if (args.getTemplateName() != null) {
					update.set(WxmpTemplateMsgMongodb.FIELD.TEMPLATE_NAME, args.getTemplateName());
				}

				if (args.getTemplateCode() != null) {
					update.set(WxmpTemplateMsgMongodb.FIELD.TEMPLATE_CODE, args.getTemplateCode());
				}

				if (args.getTemplateType() != null) {
					update.set(WxmpTemplateMsgMongodb.FIELD.TEMPLATE_TYPE, args.getTemplateType());
				}

				if (args.getTemplateText() != null) {
					update.set(WxmpTemplateMsgMongodb.FIELD.TEMPLATE_TEXT, args.getTemplateText());
				}

				if (args.getJumpUrl() != null) {
					update.set(WxmpTemplateMsgMongodb.FIELD.JUMP_URL, args.getJumpUrl());
				}

				update.set(WxmpTemplateMsgMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(WxmpTemplateMsgMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(updateQuery, update, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);

				Criteria criteria = Criteria
					.where(WxmpTemplateMsgArgMongodb.FIELD.APP_ID).is(appId)
					.and(WxmpTemplateMsgArgMongodb.FIELD.BIZ_ID).is(args.getBizId());
				Query argQuery = Query.query(criteria);
				List<WxmpTemplateMsgArgMongodb> templateMsgArgMongodbs = mongoTemplate.find(argQuery, WxmpTemplateMsgArgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);

				//删除
				List<String> deleteArgCodes = templateMsgArgMongodbs.stream().map(WxmpTemplateMsgArgMongodb::getArgCode)
					.filter(e -> !args.getArgs().stream().map(CreateWxmpTemplateMsgArgs.Arg::getArgCode).collect(Collectors.toList()).contains(e))
					.collect(Collectors.toList());

				Criteria delArgcriteria = Criteria
					.where(WxmpTemplateMsgArgMongodb.FIELD.APP_ID).is(appId)
					.and(WxmpTemplateMsgArgMongodb.FIELD.BIZ_ID).is(args.getBizId())
					.and(WxmpTemplateMsgArgMongodb.FIELD.ARG_CODE).in(deleteArgCodes);
				Query delArgQuery = Query.query(delArgcriteria);
				Update delArgUpdate = Update.update(WxmpTemplateMsgArgMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				delArgUpdate.currentDate(WxmpTemplateMsgArgMongodb.FIELD.METADATA.UPDATE_TIME);

				mongoTemplate.updateFirst(delArgQuery, delArgUpdate, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);
				List<WxmpTemplateMsgArgMongodb> deletedWxmpTemplateMsgArgMongodbList = mongoTemplate.findAllAndRemove(delArgQuery, WxmpTemplateMsgArgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);
				if (!deletedWxmpTemplateMsgArgMongodbList.isEmpty()) {
					mongoTemplate.insert(deletedWxmpTemplateMsgArgMongodbList, MongodbConstants.DeletedCollection.WXMP_TEMPLATE_MSG_ARGS);
				}

				for (int i = 0; i < args.getArgs().size(); i++) {
					CreateWxmpTemplateMsgArgs.Arg arg = args.getArgs().get(i);
					boolean argExists = templateMsgArgMongodbs.stream().filter(x -> x.getArgCode().equals(arg.getArgCode())).findFirst().isEmpty();
					//存在修改,不存在新增
					if (!argExists) {
						Criteria argsCriteria = Criteria
							.where(WxmpTemplateMsgArgMongodb.FIELD.APP_ID).is(appId)
							.and(WxmpTemplateMsgArgMongodb.FIELD.BIZ_ID).is(args.getBizId())
							.and(WxmpTemplateMsgArgMongodb.FIELD.ARG_CODE).is(arg.getArgCode());
						Query argsQuery = Query.query(argsCriteria);

						Update argUpdate = new Update();
						if (arg.getArgName() != null) {
							argUpdate.set(WxmpTemplateMsgArgMongodb.FIELD.ARG_NAME, arg.getArgName());
						}
						if (arg.getArgType() != null) {
							argUpdate.set(WxmpTemplateMsgArgMongodb.FIELD.ARG_TYPE, arg.getArgType());
						}
						if (arg.getTemplateArgCode() != null) {
							argUpdate.set(WxmpTemplateMsgArgMongodb.FIELD.TEMPLATE_ARG_CODE, arg.getTemplateArgCode());
						}
						if (arg.getDefaultColor() != null) {
							argUpdate.set(WxmpTemplateMsgArgMongodb.FIELD.DEFAULT_COLOR, arg.getDefaultColor());
						}
						argUpdate.set(WxmpTemplateMsgArgMongodb.FIELD.SORT, i + 1);
						argUpdate.set(WxmpTemplateMsgArgMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
						argUpdate.currentDate(WxmpTemplateMsgArgMongodb.FIELD.METADATA.UPDATE_TIME);
						mongoTemplate.updateFirst(argsQuery, argUpdate, WxmpTemplateMsgArgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);
					} else {
						WxmpTemplateMsgArgMongodb insertArgMongodb = WxmpTemplateMsgArgMongodb.builder()
							.appId(appId)
							.bizId(args.getBizId())
							.argCode(arg.getArgCode())
							.argName(arg.getArgName())
							.argType(arg.getArgType())
							.templateArgCode(arg.getTemplateArgCode())
							.defaultColor(arg.getDefaultColor())
							.sort(i + 1)
							.metadata(AppUserMetadataMongodb.builder()
								.createUserId(CairoSecurityContextHolder.getAppUserId())
								.updateUserId(CairoSecurityContextHolder.getAppUserId())
								.build()
							)
							.build();
						mongoTemplate.insert(insertArgMongodb, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);
					}
				}
			} catch (BusinessException e) {
				throw e;
			} catch (Exception e) {
				log.debug("modifyWxmpTemplateMsgInfo", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改微信模板信息失败");
			}
		});

	}

	/**
	 * 删除微信模板
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@Lock4j(name = "delete_wxmp_template_msg", keys = {"#appId","#args.bizId"})
	@BizLog(
		bizId = "wxmp_template_msg:delete_wxmp_template_msg",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	@SneakyThrows
	public void deleteWxmpTemplateMsg(@Valid @NotNull String appId, @Validated DeleteWxmpTemplateMsgArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria stCriteria = Criteria
					.where(WxmpTemplateMsgMongodb.FIELD.APP_ID).is(appId)
					.and(WxmpTemplateMsgMongodb.FIELD.BIZ_ID).is(args.getBizId());
				Query stQuery = Query.query(stCriteria);

				WxmpTemplateMsgMongodb wxmsTemplateMongodb = mongoTemplate.findOne(stQuery, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);
				if (wxmsTemplateMongodb == null) {
					throw new ConflictBusinessException("删除微信模板失败，模板不存在");
				}
				if (wxmsTemplateMongodb.isEnabled()) {
					throw new ConflictBusinessException("删除微信模板失败，请禁用后再删除");
				}

				Update stUpdate = Update.update(WxmpTemplateMsgMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				stUpdate.currentDate(WxmpTemplateMsgMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(stQuery, stUpdate, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);

				Criteria staCriteria = Criteria
					.where(WxmpTemplateMsgArgMongodb.FIELD.APP_ID).is(appId)
					.and(WxmpTemplateMsgArgMongodb.FIELD.BIZ_ID).is(args.getBizId());
				Query staQuery = Query.query(staCriteria);

				Update staUpdate = Update.update(WxmpTemplateMsgArgMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				staUpdate.currentDate(WxmpTemplateMsgArgMongodb.FIELD.METADATA.UPDATE_TIME);

				mongoTemplate.updateFirst(staQuery, staUpdate, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);

				WxmpTemplateMsgMongodb deletedWxmpTemplateMsgMongodb = mongoTemplate.findAndRemove(stQuery, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);
				List<WxmpTemplateMsgArgMongodb> deletedWxmpTemplateMsgArgMongodbList = mongoTemplate.findAllAndRemove(staQuery, WxmpTemplateMsgArgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);
				if (deletedWxmpTemplateMsgMongodb != null) {
					mongoTemplate.insert(deletedWxmpTemplateMsgMongodb, MongodbConstants.DeletedCollection.WXMP_TEMPLATE_MSG);
				}
				if (!deletedWxmpTemplateMsgArgMongodbList.isEmpty()) {
					mongoTemplate.insert(deletedWxmpTemplateMsgArgMongodbList, MongodbConstants.DeletedCollection.WXMP_TEMPLATE_MSG_ARGS);
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteWxmpTemplateMsg", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除微信模板失败");
			}
		});
	}


	/**
	 * 修改微信模板状态
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@Lock4j(name = "modify_wxmp_template_msg_status", keys = {"#appId","#args.bizId"})
	@BizLog(
		bizId = "wxmp_template_msg:modify_wxmp_template_msg_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	public void modifyWxmpTemplateMsgStatus(@Valid @NotNull String appId, @Validated ModifyWxmpTemplateMsgStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria
					.where(WxmpTemplateMsgMongodb.FIELD.APP_ID).is(appId)
					.and(WxmpTemplateMsgMongodb.FIELD.BIZ_ID).is(args.getBizId());
				Query query = Query.query(criteria);
				WxmpTemplateMsgMongodb node = mongoTemplate.findOne(query, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);

				if (node == null) {
					throw new ConflictBusinessException("更新微信模板状态失败，微信模板不存在");
				}

				Update update = new Update();
				if (args.getEnabled() != null) {
					update.set(WxmpTemplateMsgMongodb.FIELD.ENABLED, args.getEnabled());
				}

				update.set(WxmpTemplateMsgMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());

				update.currentDate(WxmpTemplateMsgMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateFirst = mongoTemplate.updateFirst(query, update, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);

				if (updateFirst.getModifiedCount() <= 0) {
					throw new ConflictBusinessException("更新微信模板状态失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyWxmpTemplateMsgStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("更新微信模板状态失败");
			}
		});
	}


	/**
	 * 查询微信模板列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_template_msg:get_wxmp_template_msg_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataWxmpTemplateMsg> getWxmpTemplateMsgList(@Valid @NotNull String appId, @Validated GetWxmpTemplateMsgArgs args) {
		Criteria criteria = Criteria
			.where(WxmpTemplateMsgMongodb.FIELD.APP_ID).is(appId);
		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(WxmpTemplateMsgMongodb.FIELD.TEMPLATE_NAME).regex(args.getKeyword()),
				Criteria.where(WxmpTemplateMsgMongodb.FIELD.TEMPLATE_TEXT).regex(args.getKeyword())
			);
		}

		if (args.getEnabled() != null) {
			criteria.and(WxmpTemplateMsgMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(WxmpTemplateMsgMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<WxmpTemplateMsgMongodb> wxmsTemplateMongodbList = readMongoTemplate.find(query, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);
		return getMetadataWxmpTemplateMsg(wxmsTemplateMongodbList);
	}

	/**
	 * 查询微信模板分页列表
	 *
	 * @param appId appId
	 * @param args  query args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_template_msg:get_wxmp_template_msg_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataWxmpTemplateMsg> getWxmpTemplateMsgPageList(@NotNull String appId, @Validated GetWxmpTemplateMsgArgs args) {
		Criteria criteria = Criteria
			.where(WxmpTemplateMsgMongodb.FIELD.APP_ID).is(appId);
		if (args.getKeyword() != null) {
			criteria.orOperator(
				Criteria.where(WxmpTemplateMsgMongodb.FIELD.TEMPLATE_NAME).regex(args.getKeyword()),
				Criteria.where(WxmpTemplateMsgMongodb.FIELD.TEMPLATE_TEXT).regex(args.getKeyword())
			);
		}

		if (args.getEnabled() != null) {
			criteria.and(WxmpTemplateMsgMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getWxmpProviderId() != null) {
			criteria.and(WxmpTemplateMsgMongodb.FIELD.WXMP_PROVIDER_ID).is(args.getWxmpProviderId());
		}

		Query query = Query.query(criteria);
		long total = readMongoTemplate.count(query, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(WxmpTemplateMsgMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<WxmpTemplateMsgMongodb> wxmsTemplateMongodbList = readMongoTemplate.find(query, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);
		List<MetadataWxmpTemplateMsg> contents = getMetadataWxmpTemplateMsg(wxmsTemplateMongodbList);
		return new Page<>(args, contents, total);
	}

	/**
	 * 查询微信模板信息
	 *
	 * @param appId appId
	 * @param bizId bizId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_template_msg:get_wxmp_template_msg_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "bizId", value = "#bizId"),
		}
	)
	public MetadataWxmpTemplateMsg getWxmpTemplateMsgInfo(@Valid @NotNull String appId, @NotNull String bizId) {
		Criteria sdc = Criteria
			.where(WxmpTemplateMsgMongodb.FIELD.APP_ID).is(appId)
			.and(WxmpTemplateMsgMongodb.FIELD.BIZ_ID).is(bizId);
		Query sdq = Query.query(sdc);

		WxmpTemplateMsgMongodb wxmsTemplateMongodb = readMongoTemplate.findOne(sdq, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);

		if (wxmsTemplateMongodb == null) return null;

		Set<String> userIds = Stream.of(wxmsTemplateMongodb.getMetadata()).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		// appMap
		List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
			.appIds(Collections.singletonList(wxmsTemplateMongodb.getAppId()))
			.build());
		Map<String, App> appMap = Optional.ofNullable(appList)
			.map(x -> x.stream().collect(Collectors.toMap(App::getAppId, z -> z))).orElse(Collections.emptyMap());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);

		return WxmpTemplateMsgConverter.convertMetadataWxmpTemplateMsg(wxmsTemplateMongodb, appMap, Collections.emptyMap(), metadataUserMap);
	}

	/**
	 * 查询微信模板信息
	 *
	 * @param appId appId
	 * @param bizId bizId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_template_msg:get_wxmp_template_msg_detail_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "bizId", value = "#bizId"),
		}
	)
	public MetadataWxmpTemplateMsg getWxmpTemplateMsgDetailInfo(@Valid @NotNull String appId, @NotNull String bizId) {
		Criteria smCriteria = Criteria
			.where(WxmpTemplateMsgMongodb.FIELD.APP_ID).is(appId)
			.and(WxmpTemplateMsgMongodb.FIELD.BIZ_ID).is(bizId);
		Query smQuery = Query.query(smCriteria);

		WxmpTemplateMsgMongodb wxmsTemplateMongodb = readMongoTemplate.findOne(smQuery, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);

		Criteria smaCriteria = Criteria
			.where(WxmpTemplateMsgArgMongodb.FIELD.APP_ID).is(appId)
			.and(WxmpTemplateMsgArgMongodb.FIELD.BIZ_ID).is(bizId);
		Query smaQuery = Query.query(smaCriteria);
		smaQuery.with(Sort.by(Sort.Order.asc(WxmpTemplateMsgArgMongodb.FIELD.SORT)));

		List<WxmpTemplateMsgArgMongodb> wxmsTemplateArgMongodbList = readMongoTemplate.find(smaQuery, WxmpTemplateMsgArgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);

		if (wxmsTemplateMongodb == null) return null;
		List<AppUserMetadataMongodb> metadataUserMongodbList = Stream.of(wxmsTemplateMongodb.getMetadata()).collect(Collectors.toList());

		// appMap
		List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
			.appIds(Collections.singletonList(wxmsTemplateMongodb.getAppId()))
			.build());
		Map<String, App> appMap = Optional.ofNullable(appList)
			.map(x -> x.stream().collect(Collectors.toMap(App::getAppId, z -> z))).orElse(Collections.emptyMap());
		Set<String> userIds = metadataUserMongodbList.stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);

		return WxmpTemplateMsgConverter.convertMetadataWxmpTemplateMsg(wxmsTemplateMongodb, appMap, Collections.singletonMap(bizId, wxmsTemplateArgMongodbList), metadataUserMap);
	}

	private List<MetadataWxmpTemplateMsg> getMetadataWxmpTemplateMsg(List<WxmpTemplateMsgMongodb> mongodbList) {

		Set<String> userIds = mongodbList.stream().map(WxmpTemplateMsgMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		// appMap
		Map<String, App> appMap;
		List<String> appIds = mongodbList.stream().map(WxmpTemplateMsgMongodb::getAppId).distinct().collect(Collectors.toList());
		List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
			.appIds(appIds)
			.build());
		if (!appIds.isEmpty()) {
			appMap = Optional.ofNullable(appList).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(App::getAppId, g -> g));
		} else {
			appMap = Collections.emptyMap();
		}
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);

		return mongodbList.stream()
			.map(x -> WxmpTemplateMsgConverter.convertMetadataWxmpTemplateMsg(x, appMap, Collections.emptyMap(), metadataUserMap))
			.collect(Collectors.toList());
	}
}
