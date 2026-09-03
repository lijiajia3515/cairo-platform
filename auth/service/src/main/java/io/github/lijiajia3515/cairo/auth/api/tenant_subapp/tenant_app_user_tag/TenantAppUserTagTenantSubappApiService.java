package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_user_tag;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTagMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.BasicTenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_tag.MetadataTenantAppUserTag;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_tag.TenantAppUserTagCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_tag.TenantAppUserTagConverter;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user_tag.CreateTenantAppUserTagMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user_tag.DeletedTenantAppUserTagMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user_tag.ModifiedTenantAppUserTagInfoMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user_tag.ModifiedTenantAppUserTagStatusMessage;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.CreateUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.DeleteUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.GetUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.ModifyUserTagInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.ModifyUserTagStatusArgs;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.Field;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * [tenant_subapp_user/api] tenant app user tag service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserTagTenantSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;

	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	private final ObjectMapper objectMapper;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final TenantAppUserTagCommonService tenantAppUserTagCommonService;


	public TenantAppUserTagTenantSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
													  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													  TransactionTemplate transactionTemplate,
													  RabbitTemplate rabbitTemplate,
													  CairoRabbitmqTool cairoRabbitmqTool,
													  TenantAppUserCommonService tenantAppUserCommonService,
													  TenantAppUserTagCommonService tenantAppUserTagCommonService, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
		this.tenantAppUserTagCommonService = tenantAppUserTagCommonService;
	}

	/**
	 * get tenant user tag list
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 * @return user tag list
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_tag:get_tenant_app_user_tag_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	List<MetadataTenantAppUserTag> getTenantAppUserTagList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetUserTagArgs args) {

		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.desc(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<TenantAppUserTagMongodb> userTags = mongoTemplate.find(query, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);
		log.debug("[tenant_app_user_tag][get_tenant_app_user_tag_list] query: {}", userTags);

		return getTenantAppUserTagList(tenantId, appId, userTags);
	}


	/**
	 * get tenant app user tag page list
	 *
	 * @return return user tag page list
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_tag:get_tenant_app_user_tag_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataTenantAppUserTag> getTenantAppUserTagPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetUserTagArgs args) {

		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query.query(criteria);

		long total = mongoTemplate.count(query, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);

		query.with(args.pageable());
		query.with(Sort.by(
			Sort.Order.desc(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<TenantAppUserTagMongodb> ms = mongoTemplate.find(query, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);
		log.debug("[tenant_app_user_tag][get_tenant_app_user_tag_page_list] query: {}", ms);

		List<MetadataTenantAppUserTag> contents = getTenantAppUserTagList(tenantId, appId, ms);
		return new Page<>(args, contents, total);
	}

	/**
	 * get user tag by tag id
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param tagId    tagId
	 * @return a user tag
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_tag:get_tenant_app_user_tag_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "tagId", value = "#tagId"),
		}
	)
	public MetadataTenantAppUserTag getTenantAppUserTagInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String tagId) {
		Criteria criteria = Criteria
			.where(TenantAppUserTagMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserTagMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserTagMongodb.FIELD.TAG_ID).is(tagId);

		Query query = Query.query(criteria);
		return Optional.ofNullable(readMongoTemplate.findOne(query, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG))
			.flatMap(m -> getTenantAppUserTagList(tenantId, appId, Collections.singletonList(m)).stream().findFirst())
			.orElseThrow(() -> new ConflictBusinessException("用户标签不存在"));
	}

	/**
	 * 创建用户标签
	 *
	 * @param args args
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_app_user_tag:create_tenant_app_user_tag",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createTenantAppUserTag(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated CreateUserTagArgs args) {
		Criteria criteria = Criteria
			.where(TenantAppUserTagMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserTagMongodb.FIELD.APP_ID).is(args)
			.and(TenantAppUserTagMongodb.FIELD.TAG_ID).is(args.getTagId());
		boolean exists = mongoTemplate.exists(Query.query(criteria), MongodbConstants.Collection.TENANT_APP_USER_TAG);
		if (exists) {
			throw new ConflictBusinessException(String.format("tagId已存在: %s", args.getTagId()));
		} else {
			TenantAppUserTagMongodb insertedUserTag = transactionTemplate.execute(status -> {
				try {
					TenantAppUserTagMongodb insertTenantAppUserTagMongodb = TenantAppUserTagMongodb.builder()
						.tenantId(tenantId)
						.appId(appId)
						.tagId(args.getTagId())
						.tagName(args.getTagName())
						.enabled(true)
						.metadata(TenantAppUserMetadataMongodb.builder()
							.createUserId(CairoSecurityContextHolder.getTenantAppUserId())
							.updateUserId(CairoSecurityContextHolder.getTenantAppUserId())
							.build()
						)
						.build();

					return mongoTemplate.insert(insertTenantAppUserTagMongodb, MongodbConstants.Collection.TENANT_APP_USER_TAG);
				} catch (Exception e) {
					log.debug("createTenantAppUserTag", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("创建用户标签失败");
				}
			});

			if (insertedUserTag == null) {
				throw new ConflictBusinessException("创建企业用户标签失败");
			}

			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_APP_USER_TAG, tenantId, appId),
				objectMapper.writeValueAsString(
					CreateTenantAppUserTagMessage.builder()
						.tenantId(tenantId)
						.appId(appId)
						.tagId(insertedUserTag.getTagId())
						.tagName(insertedUserTag.getTagName())
						.eventUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.eventTime(insertedUserTag.getMetadata().getCreateTime())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
			log.debug("[tenant_app_user_tag][create_tenant_app_user_tag] result -> {} ", insertedUserTag);
		}

	}

	/**
	 * 修改标签信息
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_user_tag_info", keys = {"#tenantId", "#appId", "#args.tagId"})
	@BizLog(
		bizId = "tenant_app_user_tag:modify_tenant_app_user_tag_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void modifyTenantAppUserTagInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyUserTagInfoArgs args) {
		TenantAppUserTagMongodb tenantAppUserTagMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppUserTagMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserTagMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserTagMongodb.FIELD.TAG_ID).is(args.getTagId())
				);

				Update update = Update.update(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_TIME);

				if (args.getTagName() != null) {
					update.set(TenantAppUserTagMongodb.FIELD.TAG_NAME, args.getTagName());
				}
				return mongoTemplate.findAndModify(query, update, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);
			} catch (Exception e) {
				log.debug("modifyTenantAppUserTagInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业用户标签失败");
			}
		});

		if (tenantAppUserTagMongodb != null) {
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_TENANT_APP_USER_TAG_INFO, tenantId, appId),
				objectMapper.writeValueAsString(
					ModifiedTenantAppUserTagInfoMessage.builder()
						.tenantId(tenantId)
						.appId(appId)
						.tagId(args.getTagId())
						.tagName(args.getTagName())
						.eventUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}

	/**
	 * modify tenant user tag status
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_user_tag_status", keys = {"#tenantId", "#appId", "#args.tagId"})
	@BizLog(
		bizId = "tenant_app_user_tag:modify_tenant_app_user_tag_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void modifyTenantAppUserTagStatus(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyUserTagStatusArgs args) {
		TenantAppUserTagMongodb tenantAppUserTagMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query
					.query(Criteria
						.where(TenantAppUserTagMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(TenantAppUserTagMongodb.FIELD.APP_ID).is(appId)
						.and(TenantAppUserTagMongodb.FIELD.TAG_ID).is(args.getTagId())
					);
				Update update = new Update();
				update.set(TenantAppUserTagMongodb.FIELD.ENABLED, args.getEnabled());
				update.set(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_TIME);


				FindAndModifyOptions findAndModifyOptions = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(query, update, findAndModifyOptions, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);
			} catch (Exception e) {
				log.info("modifyTenantAppUserTagStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改用户标签状态失败");
			}
		});

		if (tenantAppUserTagMongodb != null) {
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_TENANT_APP_USER_TAG_STATUS, tenantId, appId),
				objectMapper.writeValueAsString(
					ModifiedTenantAppUserTagStatusMessage.builder()
						.tenantId(tenantId)
						.appId(appId)
						.tagId(args.getTagId())
						.enabled(args.getEnabled())
						.eventUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}

	/**
	 * delete user tag
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_app_user_tags", keys = {"#tenantId", "#appId"})
	@BizLog(
		bizId = "tenant_app_user_tag:delete_tenant_app_user_tag",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteTenantAppUserTag(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull DeleteUserTagArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				List<BasicTenantAppUser> existsUserList = tenantAppUserTagCommonService.existsTenantAppUserList(tenantId, appId, args.getTagIds());
				if (!existsUserList.isEmpty()) {
					String nicknames = existsUserList.stream().map(x -> String.format("\"%s\"", x.getNickname())).collect(Collectors.joining(","));
					throw new ConflictBusinessException("标签已被用户[" + nicknames + "]使用，不允许删除");
				}
				Criteria criteria = Criteria
					.where(TenantAppUserTagMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserTagMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserTagMongodb.FIELD.TAG_ID).in(args.getTagIds());

				Query.query(criteria);
				Query query = Query.query(criteria);
				Update update = new Update();
				update.currentDate(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());

				UpdateResult updateResult = mongoTemplate.updateMulti(query, update, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("删除用户标签失败");
				}
				List<TenantAppUserTagMongodb> deleteTenantAppUserTagMongodbList = mongoTemplate.findAllAndRemove(query, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);
				if (!deleteTenantAppUserTagMongodbList.isEmpty()) {
					mongoTemplate.insert(deleteTenantAppUserTagMongodbList, MongodbConstants.DeletedCollection.TENANT_APP_USER_TAG);
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteTenantAppUserTag", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除用户标签失败");
			}
		});
		args.getTagIds().forEach(tagId-> {
			try {
				rabbitTemplate.convertAndSend(
					cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
					cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_APP_USER_TAG, tenantId, appId),
					objectMapper.writeValueAsString(
						DeletedTenantAppUserTagMessage.builder()
							.tenantId(tenantId)
							.appId(appId)
							.tagId(tagId)
							.eventUserId(CairoSecurityContextHolder.getTenantAppUserId())
							.eventTime(LocalDateTime.now())
							.build()
					),
					new CorrelationData(CoreConstants.nextIdStr())
				);
			} catch (JsonProcessingException e) {
				log.warn("deleteTenantAppUserTag", e);
			}
		});


	}


	/**
	 * 聚合返回集合
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param ms       ms
	 * @return metadata user tag list
	 */
	@NewSpan
	protected List<MetadataTenantAppUserTag> getTenantAppUserTagList(String tenantId, String appId, List<TenantAppUserTagMongodb> ms) {
		Set<String> metadataUserIds = CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(ms.stream().map(TenantAppUserTagMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, TenantAppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> tenantAppUserCommonService.getUserMapByUserIds(tenantId, appId, userIds))
			.orElse(Collections.emptyMap());

		Map<String, Integer> userCountMap = new HashMap<>();
		Set<String> tagIds = ms.stream().map(TenantAppUserTagMongodb::getTagId).collect(Collectors.toSet());
		if (!tagIds.isEmpty()) {
			final Field fieldKey = Fields.field(TenantAppUserMongodb.FIELD.TAG_IDS);
			userCountMap.putAll(readMongoTemplate.aggregate(Aggregation.newAggregation(
					Aggregation.match(Criteria
						.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
						.and(fieldKey.getTarget()).elemMatch(new Criteria().in(tagIds))
					),
					Aggregation.project(Fields.from(fieldKey)),
					Aggregation.unwind(fieldKey.getName()),
					Aggregation.group(fieldKey.getName()).count().as("Num"),
					Aggregation.sort(Sort.by(Sort.Order.desc(fieldKey.getName())))
				), MongodbConstants.Collection.TENANT_APP_USER, BasicDBObject.class).getMappedResults().stream()
				.collect(Collectors.toMap(z -> z.getString("_id"), z -> z.getInt("Num"))));
		}

		return ms.stream().map(m -> TenantAppUserTagConverter.convertMetadataTenantAppUserTag(m, userCountMap, metadataUserMap)).collect(Collectors.toList());
	}

	/**
	 * 构建查询条件
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 * @return criteria
	 */

	protected Criteria buildCriteria(String tenantId, String appId, GetUserTagArgs args) {
		Criteria criteria = Criteria
			.where(TenantAppUserTagMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserTagMongodb.FIELD.APP_ID).is(appId);

		if (args.getEnabled() != null) {
			criteria.and(TenantAppUserTagMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		return criteria;
	}
}
