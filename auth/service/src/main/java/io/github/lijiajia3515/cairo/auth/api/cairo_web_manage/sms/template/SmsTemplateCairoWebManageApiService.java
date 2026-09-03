package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.sms.template;


import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.api.client.app.AppClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.CreateSmsTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.DeleteSmsTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.GetSmsTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.ModifySmsTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.ModifySmsTemplateStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsTemplateArgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.MetadataSmsTemplate;
import io.github.lijiajia3515.cairo.auth.modules.sms.template.SmsTemplateConverter;
import io.github.lijiajia3515.cairo.auth.domain.message.sms.template.DeleteSmsTemplateMessage;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

/**
 * [cairo_web_manage/api] sms template service
 */
@Slf4j
@Validated
@Component
public class SmsTemplateCairoWebManageApiService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final AppClientApiService appClientApiService;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;

	SmsTemplateCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
											TransactionTemplate transactionTemplate,
											@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
											CairoSecurityProperties cairoSecurityProperties,
											AppUserCommonService appUserCommonService,
											AppClientApiService appClientApiService,
											RabbitTemplate rabbitTemplate,
											CairoRabbitmqTool cairoRabbitmqTool,
											ObjectMapper objectMapper) {
		this.appUserCommonService = appUserCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.appClientApiService = appClientApiService;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
	}

	/**
	 * 创建短信模板
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "sms_template:create_sms_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createSmsTemplate(@Valid @NotNull String appId, @Validated CreateSmsTemplateArgs args) {
		String currentUserId = CairoSecurityContextHolder.getAppUserId();
		SmsTemplateMongodb smsTemplateMongodb = SmsTemplateMongodb.builder()
			.appId(appId)
			.bizId(args.getBizId())
			.templateName(args.getTemplateName())
			.templateSign(args.getTemplateSign())
			.templateCode(args.getTemplateCode())
			.templateType(args.getTemplateType())
			.templateText(args.getTemplateText())
			.enabled(false)
			.metadata(AppUserMetadataMongodb.builder()
				.createUserId(currentUserId)
				.updateUserId(currentUserId)
				.build()
			)
			.build();
		AtomicInteger sort = new AtomicInteger(0);
		List<SmsTemplateArgMongodb> smsTemplateArgMongodbList = Optional.ofNullable(args.getArgs()).orElse(Collections.emptyList())
			.stream().map(x -> SmsTemplateArgMongodb.builder()
				.appId(appId)
				.bizId(args.getBizId())
				.argName(x.getArgName())
				.argType(x.getArgType())
				.argCode(x.getArgCode())
				.templateArgCode(x.getTemplateArgCode())
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
				mongoTemplate.insert(smsTemplateMongodb, MongodbConstants.Collection.SMS_TEMPLATE);
				mongoTemplate.insert(smsTemplateArgMongodbList, MongodbConstants.Collection.SMS_TEMPLATE_ARG);
			} catch (Exception e) {
				log.debug("createSmsTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建短信模板异常");
			}
		});
	}

	/**
	 * 修改短信模板信息
	 *
	 * @param appId 应用ID
	 * @param args  参数
	 */
	@Caching(
		evict = {
			@CacheEvict(cacheNames = CairoAuthRedisConstants.Keys.SMS_TEMPLATE, key = "#appId+ ':'+ #args.bizId")
		}
	)
	@Lock4j(name = "modify_sms_template_info", keys = {"#appId","#args.bizId"})
	@BizLog(
		bizId = "sms_template:modify_sms_template_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	public void modifySmsTemplateInfo(@Valid @NotNull String appId, @Validated ModifySmsTemplateInfoArgs args) {
		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Criteria existsCriteria = Criteria
					.where(SmsTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(SmsTemplateMongodb.FIELD.BIZ_ID).is(args.getBizId())
					.and(SmsTemplateMongodb.FIELD.ENABLED).is(false);
				Query existsQuery = Query.query(existsCriteria);
				boolean exists = mongoTemplate.exists(existsQuery, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);
				if (!exists) {
					throw new ConflictBusinessException("请禁用后在进行编辑操作");
				}
				Criteria updateCriteria = Criteria
					.where(SmsTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(SmsTemplateMongodb.FIELD.BIZ_ID).is(args.getBizId());
				Query updateQuery = Query.query(updateCriteria);
				Update update = new Update();

				if (args.getTemplateName() != null) {
					update.set(SmsTemplateMongodb.FIELD.TEMPLATE_NAME, args.getTemplateName());
				}

				if (args.getTemplateSign() != null) {
					update.set(SmsTemplateMongodb.FIELD.TEMPLATE_SIGN, args.getTemplateSign());
				}

				if (args.getTemplateCode() != null) {
					update.set(SmsTemplateMongodb.FIELD.TEMPLATE_CODE, args.getTemplateCode());
				}

				if (args.getTemplateType() != null) {
					update.set(SmsTemplateMongodb.FIELD.TEMPLATE_TYPE, args.getTemplateType());
				}

				if (args.getTemplateText() != null) {
					update.set(SmsTemplateMongodb.FIELD.TEMPLATE_TEXT, args.getTemplateText());
				}

				update.set(SmsTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(SmsTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(updateQuery, update, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);

				for (int i = 0; i < args.getArgs().size(); i++) {
					CreateSmsTemplateArgs.Arg arg = args.getArgs().get(i);
					Criteria criteria = Criteria
						.where(SmsTemplateArgMongodb.FIELD.APP_ID).is(appId)
						.and(SmsTemplateArgMongodb.FIELD.BIZ_ID).is(args.getBizId())
						.and(SmsTemplateArgMongodb.FIELD.ARG_CODE).is(arg.getArgCode());
					Query argQuery = Query.query(criteria);
					boolean argExists = mongoTemplate.exists(argQuery, SmsTemplateArgMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE_ARG);
					if (argExists) {
						Update argUpdate = new Update();
						if (arg.getArgName() != null) {
							argUpdate.set(SmsTemplateArgMongodb.FIELD.ARG_NAME, arg.getArgName());
						}
						if (arg.getArgType() != null) {
							argUpdate.set(SmsTemplateArgMongodb.FIELD.ARG_TYPE, arg.getArgType());
						}
						if (arg.getTemplateArgCode() != null) {
							argUpdate.set(SmsTemplateArgMongodb.FIELD.TEMPLATE_ARG_CODE, arg.getTemplateArgCode());
						}
						argUpdate.set(SmsTemplateArgMongodb.FIELD.SORT, i + 1);
						argUpdate.set(SmsTemplateArgMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
						argUpdate.currentDate(SmsTemplateArgMongodb.FIELD.METADATA.UPDATE_TIME);
						mongoTemplate.updateFirst(argQuery, argUpdate, SmsTemplateArgMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE_ARG);
					} else {
						SmsTemplateArgMongodb insertArgMongodb = SmsTemplateArgMongodb.builder()
							.appId(appId)
							.bizId(args.getBizId())
							.argCode(arg.getArgCode())
							.argName(arg.getArgName())
							.argType(arg.getArgType())
							.templateArgCode(arg.getTemplateArgCode())
							.sort(i + 1)
							.metadata(AppUserMetadataMongodb.builder()
								.createUserId(CairoSecurityContextHolder.getAppUserId())
								.updateUserId(CairoSecurityContextHolder.getAppUserId())
								.build()
							)
							.build();
						mongoTemplate.insert(insertArgMongodb, MongodbConstants.Collection.SMS_TEMPLATE_ARG);
					}
				}
			} catch (BusinessException e) {
				throw e;
			} catch (Exception e) {
				log.debug("modifySmsTemplateInfo", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改短信模板信息失败");
			}
		});

	}

	/**
	 * 删除短信模板
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@Caching(
		evict = {
			@CacheEvict(cacheNames = CairoAuthRedisConstants.Keys.SMS_TEMPLATE, key = "#appId+ ':'+ #args.bizId")
		}
	)
	@Lock4j(name = "delete_sms_template", keys = {"#appId","#args.bizId"})
	@BizLog(
		bizId = "sms_template:delete_sms_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	@SneakyThrows
	public void deleteSmsTemplate(@Valid @NotNull String appId, @Validated DeleteSmsTemplateArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria stCriteria = Criteria
					.where(SmsTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(SmsTemplateMongodb.FIELD.BIZ_ID).is(args.getBizId());
				Query stQuery = Query.query(stCriteria);

				SmsTemplateMongodb smsTemplateMongodb = mongoTemplate.findOne(stQuery, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);
				if (smsTemplateMongodb == null) {
					throw new ConflictBusinessException("删除短信模板失败，模板不存在");
				}
				if (smsTemplateMongodb.isEnabled()) {
					throw new ConflictBusinessException("删除短信模板失败，请禁用后再删除");
				}

				Update stUpdate = Update.update(SmsTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				stUpdate.currentDate(SmsTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult stUpdateResult = mongoTemplate.updateFirst(stQuery, stUpdate, MongodbConstants.Collection.SMS_TEMPLATE);

				Criteria staCriteria = Criteria
					.where(SmsTemplateArgMongodb.FIELD.APP_ID).is(appId)
					.and(SmsTemplateArgMongodb.FIELD.BIZ_ID).is(args.getBizId());
				Query staQuery = Query.query(staCriteria);

				Update staUpdate = Update.update(SmsTemplateArgMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				staUpdate.currentDate(SmsTemplateArgMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult staUpdateResult = mongoTemplate.updateFirst(staQuery, staUpdate, MongodbConstants.Collection.SMS_TEMPLATE_ARG);

				SmsTemplateMongodb deletedSmsTemplateMongodb = mongoTemplate.findAndRemove(stQuery, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);
				List<SmsTemplateArgMongodb> deletedSmsTemplateArgMongodbList = mongoTemplate.findAllAndRemove(staQuery, SmsTemplateArgMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE_ARG);
				if (deletedSmsTemplateMongodb != null) {
					mongoTemplate.insert(deletedSmsTemplateMongodb, MongodbConstants.DeletedCollection.SMS_TEMPLATE);
				}
				if (!deletedSmsTemplateArgMongodbList.isEmpty()) {
					mongoTemplate.insert(deletedSmsTemplateArgMongodbList, MongodbConstants.DeletedCollection.SMS_TEMPLATE_ARG);
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteSmsTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除系统级字典失败");
			}
		});
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_SMS_TEMPLATE, appId),
			objectMapper.writeValueAsString(DeleteSmsTemplateMessage.builder()
				.appId(appId)
				.bizId(args.getBizId())
				.eventCairoUserId(CairoSecurityContextHolder.getAppUserId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}


	/**
	 * 修改短信模板状态
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@Caching(
		evict = {
			@CacheEvict(cacheNames = CairoAuthRedisConstants.Keys.SMS_TEMPLATE, key = "#appId+ ':'+ #args.bizId")
		}
	)
	@Lock4j(name = "modify_sms_template_item_status", keys = {"#appId","#args.bizId"})
	@BizLog(
		bizId = "sms_template:modify_sms_template_item_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	public void modifySmsTemplateStatus(@Valid @NotNull String appId, @Validated ModifySmsTemplateStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria
					.where(SmsTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(SmsTemplateMongodb.FIELD.BIZ_ID).is(args.getBizId());
				Query query = Query.query(criteria);
				SmsTemplateMongodb node = mongoTemplate.findOne(query, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);

				if (node == null) {
					throw new ConflictBusinessException("更新短信模板状态失败，短信模板不存在");
				}

				Update update = new Update();
				if (args.getEnabled() != null) {
					update.set(SmsTemplateMongodb.FIELD.ENABLED, args.getEnabled());
				}

				update.set(SmsTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());

				update.currentDate(SmsTemplateMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateFirst = mongoTemplate.updateFirst(query, update, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);

				if (updateFirst.getModifiedCount() <= 0) {
					throw new ConflictBusinessException("更新短信模板状态失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifySmsTemplateStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("更新短信模板状态失败");
			}
		});
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
		bizId = "sms_template:get_sms_template_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataSmsTemplate> getSmsTemplateList(@Valid @NotNull String appId, @Validated GetSmsTemplateArgs args) {
		Criteria criteria = Criteria
			.where(SmsTemplateMongodb.FIELD.APP_ID).is(appId);
		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(SmsTemplateMongodb.FIELD.TEMPLATE_NAME).regex(args.getKeyword()),
				Criteria.where(SmsTemplateMongodb.FIELD.TEMPLATE_TEXT).regex(args.getKeyword())
			);
		}

		if (args.getEnabled() != null) {
			criteria.and(SmsTemplateMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(SmsTemplateMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<SmsTemplateMongodb> smsTemplateMongodbList = readMongoTemplate.find(query, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);
		return getMetadataSmsTemplate(smsTemplateMongodbList);
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
		bizId = "sms_template:get_sms_template_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataSmsTemplate> getSmsTemplatePageList(@NotNull String appId, @Validated GetSmsTemplateArgs args) {
		Criteria criteria = Criteria
			.where(SmsTemplateMongodb.FIELD.APP_ID).is(appId);
		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(SmsTemplateMongodb.FIELD.TEMPLATE_NAME).regex(args.getKeyword()),
				Criteria.where(SmsTemplateMongodb.FIELD.TEMPLATE_TEXT).regex(args.getKeyword())
			);
		}

		if (args.getEnabled() != null) {
			criteria.and(SmsTemplateMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		Query query = Query.query(criteria);
		long total = readMongoTemplate.count(query, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(SmsTemplateMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<SmsTemplateMongodb> smsTemplateMongodbList = readMongoTemplate.find(query, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);
		List<MetadataSmsTemplate> contents = getMetadataSmsTemplate(smsTemplateMongodbList);
		return new Page<>(args, contents, total);
	}

	/**
	 * 查询系统级字典信息
	 *
	 * @param appId appId
	 * @param bizId bizId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sms_template:get_sms_template_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "bizId", value = "#bizId"),
		}
	)
	public MetadataSmsTemplate getSmsTemplateInfo(@Valid @NotNull String appId, @NotNull String bizId) {
		Criteria sdc = Criteria
			.where(SmsTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(SmsTemplateMongodb.FIELD.BIZ_ID).is(bizId);
		Query sdq = Query.query(sdc);

		SmsTemplateMongodb smsTemplateMongodb = readMongoTemplate.findOne(sdq, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);

		if (smsTemplateMongodb == null) return null;
		Set<String> userIds = Stream.of(smsTemplateMongodb.getMetadata()).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());

		// appMap
		List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
			.appIds(Collections.singletonList(smsTemplateMongodb.getAppId()))
			.build());
		Map<String, App> appMap = Optional.ofNullable(appList)
			.map(x -> x.stream().collect(Collectors.toMap(App::getAppId, z -> z))).orElse(Collections.emptyMap());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);

		return SmsTemplateConverter.convertMetadataSmsTemplate(smsTemplateMongodb, appMap, Collections.emptyMap(), metadataUserMap);
	}

	/**
	 * 查询系统级字典信息
	 *
	 * @param appId appId
	 * @param bizId bizId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sms_template:get_sms_template_detail_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "bizId", value = "#bizId"),
		}
	)
	public MetadataSmsTemplate getSmsTemplateDetailInfo(@Valid @NotNull String appId, @NotNull String bizId) {
		Criteria smCriteria = Criteria
			.where(SmsTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(SmsTemplateMongodb.FIELD.BIZ_ID).is(bizId);
		Query smQuery = Query.query(smCriteria);

		SmsTemplateMongodb smsTemplateMongodb = readMongoTemplate.findOne(smQuery, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);

		Criteria smaCriteria = Criteria
			.where(SmsTemplateArgMongodb.FIELD.APP_ID).is(appId)
			.and(SmsTemplateArgMongodb.FIELD.BIZ_ID).is(bizId);
		Query smaQuery = Query.query(smaCriteria);
		smaQuery.with(Sort.by(Sort.Order.asc(SmsTemplateArgMongodb.FIELD.SORT)));

		List<SmsTemplateArgMongodb> smsTemplateArgMongodbList = readMongoTemplate.find(smaQuery, SmsTemplateArgMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE_ARG);

		if (smsTemplateMongodb == null) return null;
		List<AppUserMetadataMongodb> metadataUserMongodbList = Stream.of(smsTemplateMongodb.getMetadata()).collect(Collectors.toList());

		// appMap
		List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
			.appIds(Collections.singletonList(smsTemplateMongodb.getAppId()))
			.build());
		Map<String, App> appMap = Optional.ofNullable(appList)
			.map(x -> x.stream().collect(Collectors.toMap(App::getAppId, z -> z))).orElse(Collections.emptyMap());
		Set<String> userIds = metadataUserMongodbList.stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);

		return SmsTemplateConverter.convertMetadataSmsTemplate(smsTemplateMongodb, appMap, Collections.singletonMap(bizId, smsTemplateArgMongodbList), metadataUserMap);
	}

	private List<MetadataSmsTemplate> getMetadataSmsTemplate(List<SmsTemplateMongodb> mongodbList) {
		Set<String> userIds = mongodbList.stream().map(SmsTemplateMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());

		// appMap
		Map<String, App> appMap;
		List<String> appIds = mongodbList.stream().map(SmsTemplateMongodb::getAppId).distinct().collect(Collectors.toList());
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
			.map(x -> SmsTemplateConverter.convertMetadataSmsTemplate(x, appMap, Collections.emptyMap(), metadataUserMap))
			.collect(Collectors.toList());
	}

}
