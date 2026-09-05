package io.github.lijiajia3515.cairo.auth.api.subapp.app_user_tag;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserTagMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUserService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.BasicAppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_tag.AppUserMetadataTag;
import io.github.lijiajia3515.cairo.auth.modules.app_user_tag.AppUserTagCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user_tag.AppUserTagConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.CreateAppUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.DeleteAppUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.GetAppUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.ModifyAppUserTagInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.ModifyAppUserTagStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user_tag.CreateAppUserTagMessage;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * [subapp_user/api] user tag service
 */
@Slf4j
@Validated
@Component
public class AppUserTagSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;
	private final CairoAuthAppUserService cairoAuthAppUserService;
	private final AppUserCommonService appUserCommonService;
	private final AppUserTagCommonService appUserTagCommonService;


	public AppUserTagSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										  TransactionTemplate transactionTemplate,
										  RabbitTemplate rabbitTemplate,
										  CairoRabbitmqTool cairoRabbitmqTool,
										  AppUserCommonService appUserCommonService,
										  AppUserTagCommonService appUserTagCommonService,
										  ObjectMapper objectMapper,
										  CairoAuthAppUserService cairoAuthAppUserService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
		this.appUserCommonService = appUserCommonService;
		this.appUserTagCommonService = appUserTagCommonService;
		this.cairoAuthAppUserService = cairoAuthAppUserService;
	}

	/**
	 * get user tag list
	 *
	 * @param appId appId
	 * @param args  args
	 * @return user tag list
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user_tag:get_app_user_tag_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	List<AppUserMetadataTag> getAppUserTagList(@Valid @NotNull String appId, @Validated GetAppUserTagArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.desc(AppUserTagMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<AppUserTagMongodb> userTags = mongoTemplate.find(query, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);
		log.debug("[app_user_tag][get_app_user_tag_list] query: {}", userTags);

		return getAppUserTagList(appId, userTags);
	}


	/**
	 * get user tag page list
	 *
	 * @return return user tag page list
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user_tag:get_app_user_tag_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<AppUserMetadataTag> getAppUserTagPageList(@Valid @NotNull String appId, @Validated GetAppUserTagArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);

		long total = mongoTemplate.count(query, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);

		query.with(args.pageable());
		query.with(Sort.by(
			Sort.Order.desc(AppUserTagMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<AppUserTagMongodb> ms = mongoTemplate.find(query, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);
		log.debug("[app_user_tag][get_app_user_tag_page_list] query: {}", ms);

		List<AppUserMetadataTag> contents = getAppUserTagList(appId, ms);
		return new Page<>(args, contents, total);
	}

	/**
	 * get user tag by tag id
	 *
	 * @param appId appId
	 * @param tagId tagId
	 * @return a user tag
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user_tag:get_app_user_tag_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "tagId", value = "#tagId"),
		}
	)
	public AppUserMetadataTag getAppUserTagInfo(@Valid @NotNull String appId, @Valid @NotNull String tagId) {
		Criteria criteria = Criteria
			.where(AppUserTagMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserTagMongodb.FIELD.TAG_ID).is(tagId);

		Query query = Query.query(criteria);
		return Optional.ofNullable(readMongoTemplate.findOne(query, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG))
			.flatMap(m -> getAppUserTagList(appId, Collections.singletonList(m)).stream().findFirst())
			.orElseThrow(() -> new ConflictBusinessException("应用级用户标签不存在"));
	}

	/**
	 * 创建应用级用户标签
	 *
	 * @param args args
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "app_user_tag:create_app_user_tag",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createAppUserTag(@Valid @NotNull String appId, @Validated CreateAppUserTagArgs args) {
		Criteria criteria = Criteria
			.where(AppUserTagMongodb.FIELD.APP_ID).is(args)
			.and(AppUserTagMongodb.FIELD.TAG_ID).is(args.getTagId());
		boolean exists = mongoTemplate.exists(Query.query(criteria), MongodbConstants.Collection.APP_USER_TAG);
		if (exists) {
			throw new ConflictBusinessException(String.format("tagId已存在: %s", args.getTagId()));
		} else {
			AppUserTagMongodb insertedUserTag = transactionTemplate.execute(status -> {
				try {
					AppUserTagMongodb insertAppUserTagMongodb = AppUserTagMongodb.builder()
						.appId(appId)
						.tagId(args.getTagId())
						.tagName(args.getTagName())
						.enabled(true)
						.metadata(AppUserMetadataMongodb.builder()
							.createUserId(CairoSecurityContextHolder.getSubappUserId())
							.updateUserId(CairoSecurityContextHolder.getSubappUserId())
							.build()
						)
						.build();

					return mongoTemplate.insert(insertAppUserTagMongodb, MongodbConstants.Collection.APP_USER_TAG);
				} catch (Exception e) {
					log.debug("createAppUserTag", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("应用级用户标签创建失败");
				}
			});

			if (insertedUserTag == null) {
				throw new ConflictBusinessException("应用级用户标签创建失败");
			}

			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_APP_USER_TAG, appId),
				objectMapper.writeValueAsString(
					CreateAppUserTagMessage.builder()
						.appId(appId)
						.tagId(insertedUserTag.getTagId())
						.tagName(insertedUserTag.getTagName())
						.eventUserId(CairoSecurityContextHolder.getSubappUserId())
						.eventTime(insertedUserTag.getMetadata().getCreateTime())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
			log.debug("[app_user_tag][create_app_user_tag] result -> {} ", insertedUserTag);
		}
	}

	/**
	 * 修改标签信息
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_app_user_tag_info", keys = {"#appId", "#args.tagId"})
	@BizLog(
		bizId = "app_user_tag:modify_app_user_tag_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyAppUserTagInfo(@Valid @NotNull String appId, @Validated ModifyAppUserTagInfoArgs args) {
		AppUserTagMongodb userTagMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(AppUserTagMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserTagMongodb.FIELD.TAG_ID).is(args.getTagId())
				);

				Update update = Update.update(AppUserTagMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppUserTagMongodb.FIELD.METADATA.UPDATE_TIME);

				if (args.getTagName() != null) {
					update.set(AppUserTagMongodb.FIELD.TAG_NAME, args.getTagName());
				}
				return mongoTemplate.findAndModify(query, update, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);
			} catch (Exception e) {
				log.debug("modifyAppUserTagInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改应用级用户标签失败");
			}
		});

		if (userTagMongodb == null) {
			throw new ConflictBusinessException("修改应用级用户标签失败");
		}

		// remove cache
		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}

	/**
	 * modify user tag status
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_app_user_tag_status", keys = {"#appId", "#args.tagId"})
	@BizLog(
		bizId = "app_user_tag:modify_user_tag_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyAppUserTagStatus(@Valid @NotNull String appId, @Validated ModifyAppUserTagStatusArgs args) {
		AppUserTagMongodb userTagMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query
					.query(Criteria
						.where(AppUserTagMongodb.FIELD.APP_ID).is(appId)
						.and(AppUserTagMongodb.FIELD.TAG_ID).is(args.getTagId())
					);
				Update update = new Update();
				update.set(AppUserTagMongodb.FIELD.ENABLED, args.getEnabled());
				update.set(AppUserTagMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppUserTagMongodb.FIELD.METADATA.UPDATE_TIME);


				FindAndModifyOptions findAndModifyOptions = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(query, update, findAndModifyOptions, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);
			} catch (Exception e) {
				log.info("modifyAppUserTagStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改应用级用户标签状态失败");
			}
		});

		if (userTagMongodb == null) {
			throw new ConflictBusinessException("修改应用级用户标签状态失败");
		}

		// remove cache
		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}

	/**
	 * delete user tag
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_app_user_tags", keys = {"#appId"})
	@BizLog(
		bizId = "app_user_tag:delete_app_user_tag",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteAppUserTag(@Valid @NotNull String appId, @Valid @NotNull DeleteAppUserTagArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				List<BasicAppUser> existsUserList = appUserTagCommonService.existsAppUserList(appId, args.getTagIds());
				if (!existsUserList.isEmpty()) {
					String nicknames = existsUserList.stream().map(x -> String.format("\"%s\"", x.getNickname())).collect(Collectors.joining(","));
					throw new ConflictBusinessException("标签已被使用应用级用户[" + nicknames + "]，不允许删除");
				}
				Criteria criteria = Criteria
					.where(AppUserTagMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserTagMongodb.FIELD.TAG_ID).in(args.getTagIds());

				Query.query(criteria);
				Query query = Query.query(criteria);
				Update update = new Update();
				update.currentDate(AppUserTagMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(AppUserTagMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				UpdateResult updateResult = mongoTemplate.updateMulti(query, update, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("删除应用级用户标签失败");
				}
				List<AppUserTagMongodb> deleteAppUserTagMongodbList = mongoTemplate.findAllAndRemove(query, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);
				if (!deleteAppUserTagMongodbList.isEmpty()) {
					mongoTemplate.insert(deleteAppUserTagMongodbList, MongodbConstants.DeletedCollection.APP_USER_TAG);
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteAppUserTag", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除应用级用户标签失败");
			}
		});

		// remove cache
		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}


	/**
	 * 聚合返回集合
	 *
	 * @param appId appId
	 * @param ms    ms
	 * @return metadata user tag list
	 */
	@NewSpan
	protected List<AppUserMetadataTag> getAppUserTagList(String appId, List<AppUserTagMongodb> ms) {
		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(AppUserTagMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(appId, userIds))
			.orElse(Collections.emptyMap());

		Map<String, Integer> userCountMap = new HashMap<>();
		Set<String> tagIds = ms.stream().map(AppUserTagMongodb::getTagId).collect(Collectors.toSet());
		if (!tagIds.isEmpty()) {
			final Field fieldKey = Fields.field(AppUserMongodb.FIELD.TAG_IDS);
			userCountMap.putAll(readMongoTemplate.aggregate(Aggregation.newAggregation(
					Aggregation.match(Criteria
						.where(AppUserMongodb.FIELD.APP_ID).is(appId)
						.and(fieldKey.getTarget()).elemMatch(new Criteria().in(tagIds))
					),
					Aggregation.project(Fields.from(fieldKey)),
					Aggregation.unwind(fieldKey.getName()),
					Aggregation.group(fieldKey.getName()).count().as("Num"),
					Aggregation.sort(Sort.by(Sort.Order.desc(fieldKey.getName())))
				), MongodbConstants.Collection.APP_USER, BasicDBObject.class).getMappedResults().stream()
				.collect(Collectors.toMap(z -> z.getString("_id"), z -> z.getInt("Num"))));
		}

		return ms.stream().map(m -> AppUserTagConverter.convertAppUserMetadataTag(m, userCountMap, metadataUserMap)).collect(Collectors.toList());
	}

	/**
	 * 构建查询条件
	 *
	 * @param appId appId
	 * @param args  args
	 * @return criteria
	 */

	protected Criteria buildCriteria(String appId, GetAppUserTagArgs args) {
		Criteria criteria = Criteria
			.where(AppUserTagMongodb.FIELD.APP_ID).is(appId);

		if (args.getEnabled() != null) {
			criteria.and(AppUserTagMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		return criteria;
	}
}
