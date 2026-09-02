package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.notify.cairo_web_manage;


import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.mongodb.serial.SerialService;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.SerialConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.CreateNotifyTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.DeleteNotifyTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.GetNotifyTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.ModifyNotifyTemplateStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.ModifyNotificationTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.NotifyTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.notify.NotifyTemplateArgsMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.notify.NotifyTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.category.NotifyCategory;
import io.github.lijiajia3515.cairo.auth.modules.notify.category.NotifyCategoryCommonService;
import io.github.lijiajia3515.cairo.auth.modules.notify.category.args.GetNotifyCategoryArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.template.MetadataNotifyTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.template.NotifyArgsTypes;
import io.github.lijiajia3515.cairo.auth.modules.notify.template.common.NotifyTemplateCommonService;
import io.github.lijiajia3515.cairo.auth.modules.notify.template.common.NotifyTemplateConverter;
import io.github.lijiajia3515.cairo.auth.domain.message.notify.template.DeleteNotifyTemplateMessage;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * [cairo_web_manage/api] sms template service
 */
@Slf4j
@Validated
@Component
public class NotifyTemplateCairoWebManageApiService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final AppUserCommonService appUserCommonService;
	private final NotifyCategoryCommonService categoryCommonService;
	private final NotifyTemplateCommonService templateCommonService;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;
	private final SerialService serialService;

	NotifyTemplateCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
														TransactionTemplate transactionTemplate,
														@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
														CairoSecurityProperties cairoSecurityProperties,
														AppUserCommonService appUserCommonService,
														NotifyCategoryCommonService categoryCommonService, NotifyTemplateCommonService templateCommonService,
														RabbitTemplate rabbitTemplate,
														CairoRabbitmqTool cairoRabbitmqTool,
														ObjectMapper objectMapper, SerialService serialService) {
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.appUserCommonService = appUserCommonService;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.categoryCommonService = categoryCommonService;
		this.templateCommonService = templateCommonService;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
		this.serialService = serialService;
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
		bizId = "notify_template:get_notify_template_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataNotifyTemplate> getNotifyTemplateList(@Valid @NotNull String appId, @Validated GetNotifyTemplateArgs args) {
		Criteria criteria = Criteria
			.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId);

		if (args.getCategoryIds() != null && !args.getCategoryIds().isEmpty()) {
			criteria.in(NotifyTemplateMongodb.FIELD.CATEGORY_ID).in(args.getCategoryIds());
		}

		if (args.getMessageTypes() != null && !args.getMessageTypes().isEmpty()) {
			criteria.in(NotifyTemplateMongodb.FIELD.MESSAGE_TYPE).in(args.getMessageTypes());
		}

		if (args.getLinkTypes() != null && !args.getLinkTypes().isEmpty()) {
			criteria.in(NotifyTemplateMongodb.FIELD.LINK_TYPE).in(args.getLinkTypes());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(NotifyTemplateMongodb.FIELD.MESSAGE_CODE).regex(args.getKeyword()),
				Criteria.where(NotifyTemplateMongodb.FIELD.TEMPLATE_NAME).regex(args.getKeyword())
			);
		}

		if (args.getEnabled() != null) {
			criteria.and(NotifyTemplateMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(SmsTemplateMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<NotifyTemplateMongodb> recordList = readMongoTemplate.find(query, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);
		return getMetadataNotifyTemplate(appId, recordList);
	}

	/**
	 * 查询通知消息模板分页列表
	 *
	 * @param appId appId
	 * @param args  query args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "notify_template:get_notify_template_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataNotifyTemplate> getNotifyTemplatePageList(@NotNull String appId, @Validated GetNotifyTemplateArgs args) {
		Criteria criteria = Criteria
			.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId);

		if (args.getCategoryIds() != null && !args.getCategoryIds().isEmpty()) {
			criteria.and(NotifyTemplateMongodb.FIELD.CATEGORY_ID).in(args.getCategoryIds());
		}

		if (args.getMessageTypes() != null && !args.getMessageTypes().isEmpty()) {
			criteria.and(NotifyTemplateMongodb.FIELD.MESSAGE_TYPE).in(args.getMessageTypes());
		}

		if (args.getLinkTypes() != null && !args.getLinkTypes().isEmpty()) {
			criteria.and(NotifyTemplateMongodb.FIELD.LINK_TYPE).in(args.getLinkTypes());
		}



		if (args.getEnabled() != null) {
			criteria.and(NotifyTemplateMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(NotifyTemplateMongodb.FIELD.MESSAGE_CODE).regex(args.getKeyword()),
				Criteria.where(NotifyTemplateMongodb.FIELD.TEMPLATE_NAME).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);
		long total = readMongoTemplate.count(query, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(NotifyTemplateMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<NotifyTemplateMongodb> mongodbList = readMongoTemplate.find(query, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);
		List<MetadataNotifyTemplate> contents = getMetadataNotifyTemplate(appId, mongodbList);
		return new Page<>(args, contents, total);
	}

	/**
	 * 查询通知消息模板信息
	 *
	 * @param appId      appId
	 * @param templateId templateId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "notify_template:get_notify_template_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "templateId", value = "#templateId"),
		}
	)
	public MetadataNotifyTemplate getNotifyTemplateInfo(@Valid @NotNull String appId, @NotNull String templateId) {
		Criteria criteria = Criteria
			.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(NotifyTemplateMongodb.FIELD.TEMPLATE_ID).is(templateId);
		Query sdq = Query.query(criteria);

		NotifyTemplateMongodb mongodb = readMongoTemplate.findOne(sdq, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);

		if (mongodb == null) return null;

		Map<String, NotifyCategory> categoryMap = new HashMap<>();
		if (mongodb.getCategoryId() != null) {
			categoryMap.putAll(categoryCommonService.getNotifyCategory(appId,
					GetNotifyCategoryArgs.builder()
						.categoryIds(Collections.singletonList(mongodb.getCategoryId()))
						.build()
				)
			);
		}

		Set<String> userIds = Stream.of(mongodb.getMetadata()).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(appId, userIds);


		return NotifyTemplateConverter.convertMetadataNotifyTemplate(mongodb, categoryMap, Collections.emptyMap(), metadataUserMap);
	}

	/**
	 * 查询通知消息模板信息
	 *
	 * @param appId      appId
	 * @param templateId templateId
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "notify_template:get_notify_template_detail_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "templateId", value = "#templateId"),
		}
	)
	public MetadataNotifyTemplate getNotifyTemplateDetailInfo(@Valid @NotNull String appId, @NotNull String templateId) {
		Criteria smCriteria = Criteria
			.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(NotifyTemplateMongodb.FIELD.TEMPLATE_ID).is(templateId);
		Query smQuery = Query.query(smCriteria);

		NotifyTemplateMongodb mongodb = readMongoTemplate.findOne(smQuery, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);

		if (mongodb == null) return null;

		Set<String> userIds = CairoAppUserTool.getMetadataUserIdStream(mongodb.getMetadata()).collect(Collectors.toSet());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(appId, userIds);


		Map<String, NotifyCategory> categoryMap = categoryCommonService
			.getNotifyCategory(appId,
				GetNotifyCategoryArgs.builder()
					.categoryIds(Collections.singletonList(mongodb.getCategoryId()))
					.build()
			);

		Criteria argsCriteria = Criteria.where(NotifyTemplateArgsMongodb.FIELD.APP_ID).is(appId)
			.and(NotifyTemplateArgsMongodb.FIELD.TEMPLATE_ID).is(templateId);

		Query argsQuery = Query.query(argsCriteria);
		argsQuery.with(Sort.by(
			Sort.Order.asc(NotifyTemplateArgsMongodb.FIELD.ARGS_TYPE),
			Sort.Order.asc(NotifyTemplateArgsMongodb.FIELD.SORT)
		));
		List<NotifyTemplateArgsMongodb> argsMongodbList = readMongoTemplate.find(argsQuery, NotifyTemplateArgsMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
		Map<String, List<NotifyTemplateArgsMongodb>> argsMap = Collections.singletonMap(templateId, argsMongodbList);

		return NotifyTemplateConverter.convertMetadataNotifyTemplate(mongodb, categoryMap, argsMap, metadataUserMap);

	}

	/**
	 * 创建通知消息模板
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "notify_template:create_notify_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createNotifyTemplate(@Valid @NotNull String appId, @Validated CreateNotifyTemplateArgs args) {
		String currentUserId = CairoSecurityContextHolder.getAppUserId();
		NotifyTemplateMongodb templateMongodb = NotifyTemplateMongodb.builder()
			.appId(appId)
			.templateId(serialService.nextStr(appId, SerialConstants.NOTIFY_TEMPLATE))
			.templateName(args.getTemplateName())
			.categoryId(args.getCategoryId())
			.messageCode(args.getMessageCode())
			.messageIcon(args.getMessageIcon())
			.messageTitle(args.getMessageTitle())
			.messageAlert(args.getMessageAlert())
			.messageType(args.getMessageType())
			.messageContent(args.getMessageContent())
			.linkType(args.getLinkType())
			.pageUrl(args.getPageUrl())
			.linkUrl(args.getLinkUrl())
			.enabled(false)
			.metadata(AppUserMetadataMongodb.builder()
				.createUserId(currentUserId)
				.updateUserId(currentUserId)
				.build()
			)
			.build();
		AtomicInteger sort = new AtomicInteger(0);
		List<NotifyTemplateArgsMongodb> alertArgsMongodbList = Optional.ofNullable(args.getAlertArgs()).orElse(Collections.emptyList())
			.stream().map(x -> NotifyTemplateArgsMongodb.builder()
				.appId(appId)
				.templateId(templateMongodb.getTemplateId())
				.argsType(NotifyArgsTypes.ALERT)
				.argsCode(x.getArgsCode())
				.argsName(x.getArgsName())
				.dataType(x.getDataType())
				.defaultValue(x.getDefaultValue())
				.metadata(AppUserMetadataMongodb.builder()
					.createUserId(currentUserId)
					.updateUserId(currentUserId)
					.build()
				)
				.sort(sort.incrementAndGet())
				.build()
			).collect(Collectors.toList());

		List<NotifyTemplateArgsMongodb> contentArgsList = Optional.ofNullable(args.getContentArgs()).orElse(Collections.emptyList())
			.stream().map(x -> NotifyTemplateArgsMongodb.builder()
				.appId(appId)
				.templateId(templateMongodb.getTemplateId())
				.argsType(NotifyArgsTypes.CONTENT)
				.argsCode(x.getArgsCode())
				.argsName(x.getArgsName())
				.dataType(x.getDataType())
				.defaultValue(x.getDefaultValue())
				.metadata(AppUserMetadataMongodb.builder()
					.createUserId(currentUserId)
					.updateUserId(currentUserId)
					.build()
				)
				.sort(sort.incrementAndGet())
				.build()
			).collect(Collectors.toList());

		List<NotifyTemplateArgsMongodb> extrasArgsList = Optional.ofNullable(args.getTemplateArgs()).orElse(Collections.emptyList())
			.stream().map(x -> NotifyTemplateArgsMongodb.builder()
				.appId(appId)
				.templateId(templateMongodb.getTemplateId())
				.argsType(NotifyArgsTypes.TEMPLATE)
				.argsCode(x.getArgsCode())
				.argsName(x.getArgsName())
				.dataType(x.getDataType())
				.defaultValue(x.getDefaultValue())
				.metadata(AppUserMetadataMongodb.builder()
					.createUserId(currentUserId)
					.updateUserId(currentUserId)
					.build()
				)
				.sort(sort.incrementAndGet())
				.build()
			).collect(Collectors.toList());

		List<NotifyTemplateArgsMongodb> argsMongodbList = new ArrayList<>();
		argsMongodbList.addAll(alertArgsMongodbList);
		argsMongodbList.addAll(contentArgsList);
		argsMongodbList.addAll(extrasArgsList);

		transactionTemplate.executeWithoutResult(status -> {
			try {
				mongoTemplate.insert(templateMongodb, MongodbConstants.Collection.NOTIFY_TEMPLATE);
				mongoTemplate.insert(argsMongodbList, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
			} catch (Exception e) {
				log.debug("createNotifyTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建通知消息模板异常");
			}
		});
	}

	/**
	 * 修改通知消息模板信息
	 *
	 * @param appId 应用ID
	 * @param args  参数
	 */
	@Lock4j(name = "modify_notify_template_info", keys = {"#appId", "#args.templateId"})
	@BizLog(
		bizId = "sms_template:modify_sms_template_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	public void modifyNotifyTemplateInfo(@Valid @NotNull String appId, @Validated ModifyNotificationTemplateInfoArgs args) {
		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Criteria existsCriteria = Criteria
					.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(NotifyTemplateMongodb.FIELD.TEMPLATE_ID).is(args.getTemplateId())
					.and(NotifyTemplateMongodb.FIELD.ENABLED).is(true);
				Query existsQuery = Query.query(existsCriteria);
				boolean exists = mongoTemplate.exists(existsQuery, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);
				if (exists) {
					throw new ConflictBusinessException("请禁用后在进行编辑操作");
				}
				Criteria updateCriteria = Criteria
					.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(NotifyTemplateMongodb.FIELD.TEMPLATE_ID).is(args.getTemplateId());
				Query updateQuery = Query.query(updateCriteria);
				Update update = new Update();

				if (args.getTemplateName() != null) {
					update.set(NotifyTemplateMongodb.FIELD.TEMPLATE_NAME, args.getTemplateName());
				}

				if (args.getCategoryId() != null) {
					update.set(NotifyTemplateMongodb.FIELD.CATEGORY_ID, args.getCategoryId());
				}

				if (args.getMessageCode() != null) {
					update.set(NotifyTemplateMongodb.FIELD.MESSAGE_CODE, args.getMessageCode());
				}

				if (args.getMessageIcon() != null) {
					update.set(NotifyTemplateMongodb.FIELD.MESSAGE_ICON, args.getMessageIcon());
				}

				if (args.getMessageTitle() != null) {
					update.set(NotifyTemplateMongodb.FIELD.MESSAGE_TITLE, args.getMessageTitle());
				}

				if (args.getMessageAlert() != null) {
					update.set(NotifyTemplateMongodb.FIELD.MESSAGE_ALERT, args.getMessageAlert());
				}

				if (args.getMessageContent() != null) {
					update.set(NotifyTemplateMongodb.FIELD.MESSAGE_CONTENT, args.getMessageContent());
				}

				if (args.getLinkType() != null) {
					update.set(NotifyTemplateMongodb.FIELD.LINK_TYPE, args.getLinkType());
				}

				if (args.getLinkUrl() != null) {
					update.set(NotifyTemplateMongodb.FIELD.LINK_URL, args.getLinkUrl());
				}

				if (args.getPageUrl() != null) {
					update.set(NotifyTemplateMongodb.FIELD.PAGE_URL, args.getPageUrl());
				}


				update.set(NotifyTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(NotifyTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(updateQuery, update, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);

				// alert args
				for (int i = 0; i < args.getAlertArgs().size(); i++) {
					NotifyTemplateArgs arg = args.getAlertArgs().get(i);
					Criteria criteria = Criteria
						.where(NotifyTemplateArgsMongodb.FIELD.APP_ID).is(appId)
						.and(NotifyTemplateArgsMongodb.FIELD.TEMPLATE_ID).is(args.getTemplateId())
						.and(NotifyTemplateArgsMongodb.FIELD.ARGS_TYPE).is(NotifyArgsTypes.ALERT)
						.and(NotifyTemplateArgsMongodb.FIELD.ARGS_CODE).is(arg.getArgsCode());
					Query argsQuery = Query.query(criteria);
					boolean argExists = mongoTemplate.exists(argsQuery, NotifyTemplateArgsMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
					if (argExists) {
						Update argsUpdate = new Update();
						if (arg.getArgsName() != null) {
							argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.ARGS_NAME, arg.getArgsName());
						}
						if (arg.getDataType() != null) {
							argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.DATA_TYPE, arg.getDataType());
						}
						if (arg.getDefaultValue() != null) {
							argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.DEFAULT_VALUE, arg.getDefaultValue());
						}
						argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.SORT, i + 1);
						argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
						argsUpdate.currentDate(NotifyTemplateArgsMongodb.FIELD.METADATA.UPDATE_TIME);
						mongoTemplate.updateFirst(argsQuery, argsUpdate, NotifyTemplateArgsMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
					} else {
						NotifyTemplateArgsMongodb insertArgMongodb = NotifyTemplateArgsMongodb.builder()
							.appId(appId)
							.templateId(args.getTemplateId())
							.argsType(NotifyArgsTypes.ALERT)
							.argsCode(arg.getArgsCode())
							.argsName(arg.getArgsName())
							.defaultValue(arg.getDefaultValue())
							.sort(i + 1)
							.metadata(AppUserMetadataMongodb.builder()
								.createUserId(CairoSecurityContextHolder.getAppUserId())
								.updateUserId(CairoSecurityContextHolder.getAppUserId())
								.build()
							)
							.build();
						mongoTemplate.insert(insertArgMongodb, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
					}
				}

				// content args
				for (int i = 0; i < args.getContentArgs().size(); i++) {
					NotifyTemplateArgs arg = args.getContentArgs().get(i);
					Criteria criteria = Criteria
						.where(NotifyTemplateArgsMongodb.FIELD.APP_ID).is(appId)
						.and(NotifyTemplateArgsMongodb.FIELD.TEMPLATE_ID).is(args.getTemplateId())
						.and(NotifyTemplateArgsMongodb.FIELD.ARGS_TYPE).is(NotifyArgsTypes.CONTENT)
						.and(NotifyTemplateArgsMongodb.FIELD.ARGS_CODE).is(arg.getArgsCode());
					Query argsQuery = Query.query(criteria);
					boolean argExists = mongoTemplate.exists(argsQuery, NotifyTemplateArgsMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
					if (argExists) {
						Update argsUpdate = new Update();
						if (arg.getArgsName() != null) {
							argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.ARGS_NAME, arg.getArgsName());
						}
						if (arg.getDataType() != null) {
							argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.DATA_TYPE, arg.getDataType());
						}
						if (arg.getDefaultValue() != null) {
							argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.DEFAULT_VALUE, arg.getDefaultValue());
						}
						argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.SORT, i + 1);
						argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
						argsUpdate.currentDate(NotifyTemplateArgsMongodb.FIELD.METADATA.UPDATE_TIME);
						mongoTemplate.updateFirst(argsQuery, argsUpdate, NotifyTemplateArgsMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
					} else {
						NotifyTemplateArgsMongodb insertArgMongodb = NotifyTemplateArgsMongodb.builder()
							.appId(appId)
							.templateId(args.getTemplateId())
							.argsType(NotifyArgsTypes.CONTENT)
							.argsCode(arg.getArgsCode())
							.argsName(arg.getArgsName())
							.defaultValue(arg.getDefaultValue())
							.sort(i + 1)
							.metadata(AppUserMetadataMongodb.builder()
								.createUserId(CairoSecurityContextHolder.getAppUserId())
								.updateUserId(CairoSecurityContextHolder.getAppUserId())
								.build()
							)
							.build();
						mongoTemplate.insert(insertArgMongodb, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
					}
				}

				// template args
				for (int i = 0; i < args.getTemplateArgs().size(); i++) {
					NotifyTemplateArgs arg = args.getTemplateArgs().get(i);
					Criteria criteria = Criteria
						.where(NotifyTemplateArgsMongodb.FIELD.APP_ID).is(appId)
						.and(NotifyTemplateArgsMongodb.FIELD.TEMPLATE_ID).is(args.getTemplateId())
						.and(NotifyTemplateArgsMongodb.FIELD.ARGS_TYPE).is(NotifyArgsTypes.TEMPLATE)
						.and(NotifyTemplateArgsMongodb.FIELD.ARGS_CODE).is(arg.getArgsCode());
					Query argsQuery = Query.query(criteria);
					boolean argExists = mongoTemplate.exists(argsQuery, NotifyTemplateArgsMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
					if (argExists) {
						Update argsUpdate = new Update();
						if (arg.getArgsName() != null) {
							argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.ARGS_NAME, arg.getArgsName());
						}
						if (arg.getDataType() != null) {
							argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.DATA_TYPE, arg.getDataType());
						}
						if (arg.getDefaultValue() != null) {
							argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.DEFAULT_VALUE, arg.getDefaultValue());
						}
						argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.SORT, i + 1);
						argsUpdate.set(NotifyTemplateArgsMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
						argsUpdate.currentDate(NotifyTemplateArgsMongodb.FIELD.METADATA.UPDATE_TIME);
						mongoTemplate.updateFirst(argsQuery, argsUpdate, NotifyTemplateArgsMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
					} else {
						NotifyTemplateArgsMongodb insertArgMongodb = NotifyTemplateArgsMongodb.builder()
							.appId(appId)
							.templateId(args.getTemplateId())
							.argsType(NotifyArgsTypes.TEMPLATE)
							.argsCode(arg.getArgsCode())
							.argsName(arg.getArgsName())
							.defaultValue(arg.getDefaultValue())
							.sort(i + 1)
							.metadata(AppUserMetadataMongodb.builder()
								.createUserId(CairoSecurityContextHolder.getAppUserId())
								.updateUserId(CairoSecurityContextHolder.getAppUserId())
								.build()
							)
							.build();
						mongoTemplate.insert(insertArgMongodb, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
					}
				}
			} catch (BusinessException e) {
				throw e;
			} catch (Exception e) {
				log.debug("modifyNotifyTemplateInfo", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改通知消息模板信息失败");
			}
		});

	}

	/**
	 * 删除通知消息模板
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@Lock4j(name = "delete_notify_template", keys = {"#appId", "#args.templateId"})
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
	public void deleteNotifyTemplate(@Valid @NotNull String appId, @Validated DeleteNotifyTemplateArgs args) {
		AtomicReference<NotifyTemplateMongodb> deletedNotifyTemplate = new AtomicReference<>();
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria stCriteria = Criteria
					.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(NotifyTemplateMongodb.FIELD.TEMPLATE_ID).is(args.getTemplateId());
				Query stQuery = Query.query(stCriteria);

				NotifyTemplateMongodb TemplateMongodb = mongoTemplate.findOne(stQuery, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);
				if (TemplateMongodb == null) {
					throw new ConflictBusinessException("删除通知消息模板失败（模板不存在）");
				}
				if (TemplateMongodb.isEnabled()) {
					throw new ConflictBusinessException("删除短信模板失败（请禁用后再删除）");
				}

				Update stUpdate = Update.update(NotifyTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				stUpdate.currentDate(NotifyTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult stUpdateResult = mongoTemplate.updateFirst(stQuery, stUpdate, MongodbConstants.Collection.NOTIFY_TEMPLATE);

				Criteria staCriteria = Criteria
					.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(NotifyTemplateMongodb.FIELD.TEMPLATE_ID).is(args.getTemplateId());
				Query staQuery = Query.query(staCriteria);

				Update staUpdate = Update.update(NotifyTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				staUpdate.currentDate(NotifyTemplateMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult staUpdateResult = mongoTemplate.updateFirst(staQuery, staUpdate, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);

				NotifyTemplateMongodb deleteTemplateMongodb = mongoTemplate.findAndRemove(stQuery, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);
				List<NotifyTemplateArgsMongodb> deletedTemplateArgsMongodbList = mongoTemplate.findAllAndRemove(staQuery, NotifyTemplateArgsMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);
				if (deleteTemplateMongodb != null) {
					deletedNotifyTemplate.set(deleteTemplateMongodb);
					mongoTemplate.insert(deleteTemplateMongodb, MongodbConstants.DeletedCollection.NOTIFY_TEMPLATE);
				}
				if (!deletedTemplateArgsMongodbList.isEmpty()) {
					mongoTemplate.insert(deletedTemplateArgsMongodbList, MongodbConstants.DeletedCollection.NOTIFY_TEMPLATE_ARGS);
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteNotifyTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除通知消息模板失败");
			}
		});
		if (deletedNotifyTemplate.get() != null) {
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_NOTIFY_TEMPLATE, appId),
				objectMapper.writeValueAsString(DeleteNotifyTemplateMessage.builder()
					.appId(appId)
					.templateId(args.getTemplateId())
					.messageCode(deletedNotifyTemplate.get().getMessageCode())
					.eventCairoUserId(CairoSecurityContextHolder.getAppUserId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}

	}

	/**
	 * 修改通知消息模板状态
	 *
	 * @param appId appId
	 * @param args  args
	 */

	@Lock4j(name = "modify_notify_template_status", keys = {"#appId", "#args.templateId"})
	@BizLog(
		bizId = "notify_template:modify_notify_template_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	public void modifyNotifyTemplateStatus(@Valid @NotNull String appId, @Validated ModifyNotifyTemplateStatusArgs args) {
		NotifyTemplateMongodb modifiedRecord = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(NotifyTemplateMongodb.FIELD.TEMPLATE_ID).is(args.getTemplateId());
				Query query = Query.query(criteria);
				NotifyTemplateMongodb node = mongoTemplate.findOne(query, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);

				if (node == null) {
					throw new ConflictBusinessException("更新通知消息模板状态失败(模板不存在)");
				}

				Update update = new Update();
				if (args.getEnabled() != null) {
					update.set(NotifyTemplateMongodb.FIELD.ENABLED, args.getEnabled());
				}

				update.set(NotifyTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(NotifyTemplateMongodb.FIELD.METADATA.UPDATE_TIME);

				return mongoTemplate.findAndModify(query, update, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyNotifyTemplateStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("更新通知消息模板状态失败");
			}
		});
		if (args.getEnabled() && modifiedRecord != null && modifiedRecord.isEnabled()) {
			templateCommonService.clearCache(appId, modifiedRecord.getMessageCode());
		}
	}


	private List<MetadataNotifyTemplate> getMetadataNotifyTemplate(String appId, List<NotifyTemplateMongodb> mongodbList) {
		Set<String> userIds = mongodbList.stream().map(NotifyTemplateMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(appId, userIds);

		Map<String, NotifyCategory> categoryMap = new HashMap<>();
		List<String> categoryIds = mongodbList.stream().map(NotifyTemplateMongodb::getCategoryId).distinct().toList();
		if (!categoryIds.isEmpty()) {
			categoryMap.putAll(categoryCommonService.getNotifyCategory(appId, GetNotifyCategoryArgs.builder().categoryIds(categoryIds).build()));
		}


		return mongodbList.stream()
			.map(x -> NotifyTemplateConverter.convertMetadataNotifyTemplate(x, categoryMap, Collections.emptyMap(), metadataUserMap))
			.collect(Collectors.toList());
	}

}
