package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.notify.template;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.mongodb.serial.SerialService;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.SerialConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.CreateNotifyCategoryArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.DeleteNotifyCategoryArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.GetNotifyCategoryArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.ModifyNotifyCategoryInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.ModifyNotifyCategoryStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.notify.NotifyCategoryMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.category.MetadataNotifyCategory;
import io.github.lijiajia3515.cairo.auth.modules.notify.category.NotifyCategoryConverter;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Validated
@Component
public class NotifyCategoryCairoWebManageApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final SerialService serialService;

	private final AppUserCommonService appUserCommonService;


	public NotifyCategoryCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
															   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
															   TransactionTemplate transactionTemplate, SerialService serialService,
															   AppUserCommonService appUserCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.serialService = serialService;
		this.appUserCommonService = appUserCommonService;
	}

	@NewSpan
	@BizLog(
		bizId = "notify_category:get_notify_category_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataNotifyCategory> getNotifyCategoryList(String appId, GetNotifyCategoryArgs args) {
		Criteria criteria = buildFindCriteria(appId, args);
		Query query = Query.query(criteria).with(sort());
		List<NotifyCategoryMongodb> pms = readMongoTemplate.find(query, NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);
		return find(appId, pms, Collections.emptyMap());
	}

	@NewSpan
	@BizLog(
		bizId = "notify_category:get_notify_category_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataNotifyCategory> getNotifyCategoryPageList(String appId, GetNotifyCategoryArgs args) {
		Criteria criteria = buildFindCriteria(appId, args);
		Query query = Query.query(criteria).with(sort());
		long count = readMongoTemplate.count(query, NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);
		List<NotifyCategoryMongodb> pms = readMongoTemplate.find(query.with(args.pageable()), NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);
		List<MetadataNotifyCategory> notifyCategories = find(appId, pms, Collections.emptyMap());
		return new Page<>(args, notifyCategories, count);
	}

	public Criteria buildFindCriteria(String appId, GetNotifyCategoryArgs args) {
		Criteria criteria = Criteria.where(NotifyCategoryMongodb.FIELD.APP_ID).is(appId);
		Optional.ofNullable(args.getCategoryIds()).filter(x -> !x.isEmpty()).ifPresent(x -> criteria.and(NotifyCategoryMongodb.FIELD.CATEGORY_ID).in(x));
		Optional.ofNullable(args.getKeyword()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(NotifyCategoryMongodb.FIELD.CATEGORY_NAME).regex(x));
		Optional.ofNullable(args.getEnabled()).ifPresent(x -> criteria.and(NotifyCategoryMongodb.FIELD.ENABLED).is(x));

		return criteria;
	}

	private List<MetadataNotifyCategory> find(String appId, List<NotifyCategoryMongodb> mongodbList, Map<String, String> extensionMap) {
		Set<String> userIds = mongodbList.stream().map(NotifyCategoryMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(appId, userIds);
		return mongodbList.stream()
			.map(x -> NotifyCategoryConverter.convertMetadataNotifyCategory(x, metadataUserMap))
			.collect(Collectors.toList());
	}

	@NewSpan
	@BizLog(
		bizId = "notify_category:create_notify_category",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createNotifyCategory(String appId, CreateNotifyCategoryArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria
					.where(NotifyCategoryMongodb.FIELD.APP_ID).is(appId)
					.and(NotifyCategoryMongodb.FIELD.CATEGORY_NAME).is(args.getCategoryName());
				Query query = Query.query(criteria);
				boolean exists = mongoTemplate.exists(query, NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);
				if (exists) {
					throw new ConflictBusinessException("通知消息类别名称已存在");
				}
				NotifyCategoryMongodb messageCategoryMongodb = NotifyCategoryMongodb.builder()
					.categoryId("" + serialService.next(appId, SerialConstants.NOTIFY_CATEGORY))
					.appId(appId)
					.categoryName(args.getCategoryName())
					.categoryIcon(args.getCategoryIcon())
					.enabled(true)
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getAppUserId())
						.createTime(LocalDateTime.now())
						.updateUserId(CairoSecurityContextHolder.getAppUserId())
						.updateTime(LocalDateTime.now())
						.build())
					.build();
				mongoTemplate.insert(messageCategoryMongodb, MongodbConstants.Collection.NOTIFY_CATEGORY);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (RuntimeException e) {
				log.debug("createNotifyCategory", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建通知消息类别失败");
			}
		});
	}

	@NewSpan
	@Lock4j(name = "modify_notify_category_info", keys = {"#appId", "#args.categoryId"})
	@BizLog(
		bizId = "notify_category:modify_notify_category_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyNotifyCategoryInfo(String appId, ModifyNotifyCategoryInfoArgs args) {

		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria.where(NotifyCategoryMongodb.FIELD.CATEGORY_NAME).is(args.getCategoryName())
					.and(NotifyCategoryMongodb.FIELD.APP_ID).is(appId)
					.and(NotifyCategoryMongodb.FIELD.CATEGORY_ID).ne(args.getCategoryId());
				Query query = Query.query(criteria);
				boolean exists = mongoTemplate.exists(query, NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);
				if (exists) {
					throw new ConflictBusinessException("修改通知消息类别失败（名称已存在）");
				}
				Criteria updateCriteria = Criteria.where(NotifyCategoryMongodb.FIELD.APP_ID).is(appId)
					.and(NotifyCategoryMongodb.FIELD.CATEGORY_ID).is(args.getCategoryId());

				Update update = Update.update(NotifyCategoryMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now())
					.set(NotifyCategoryMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				Optional.ofNullable(args.getCategoryName()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(NotifyCategoryMongodb.FIELD.CATEGORY_NAME, x));
				Optional.ofNullable(args.getCategoryIcon()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(NotifyCategoryMongodb.FIELD.CATEGORY_ICON, x));

				mongoTemplate.updateFirst(Query.query(updateCriteria), update, NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (RuntimeException e) {
				log.debug("modifyNotifyCategoryInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改通知消息类别失败");
			}
		});
	}

	@NewSpan
	@Lock4j(name = "modify_notify_category_status", keys = {"#appId", "#args.categoryId"})
	@BizLog(
		bizId = "notify_category:modify_notify_category_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyNotifyCategoryStatus(String appId, ModifyNotifyCategoryStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
				try {
					Criteria updateCriteria = Criteria.where(NotifyCategoryMongodb.FIELD.APP_ID).is(appId)
						.and(NotifyCategoryMongodb.FIELD.CATEGORY_ID).is(args.getCategoryId());

					Update update = Update.update(NotifyCategoryMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now())
						.set(NotifyCategoryMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
					Optional.ofNullable(args.getEnabled()).ifPresent(x -> update.set(NotifyCategoryMongodb.FIELD.ENABLED, x));

					mongoTemplate.updateFirst(Query.query(updateCriteria), update, NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);
				} catch (ConflictBusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (RuntimeException e) {
					log.debug("modifyNotifyCategoryStatus", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("修改通知消息类别状态失败");
				}
			}
		);
	}

	@NewSpan
	@Lock4j(name = "delete_notify_category", keys = {"#args.categoryIds"})
	@BizLog(
		bizId = "notify_category:delete_notify_category",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteNotifyCategory(String appId, DeleteNotifyCategoryArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
				try {
					Criteria criteria = Criteria.where(NotifyCategoryMongodb.FIELD.CATEGORY_ID).in(args.getCategoryIds())
						.and(NotifyCategoryMongodb.FIELD.APP_ID).is(appId);

					Update update = Update.update(NotifyCategoryMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now())
						.set(NotifyCategoryMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
					mongoTemplate.updateMulti(Query.query(criteria), update, NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);
					List<NotifyCategoryMongodb> removeNotifyCategoryMongodbList = mongoTemplate.findAllAndRemove(Query.query(criteria), NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);

					if (!removeNotifyCategoryMongodbList.isEmpty()) {
						mongoTemplate.insert(removeNotifyCategoryMongodbList, MongodbConstants.DeletedCollection.NOTIFY_CATEGORY);
					}
				} catch (ConflictBusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (RuntimeException e) {
					log.debug("deleteNotifyCategory", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("删除通知消息类别失败");
				}
			}
		);
	}


	public Sort sort() {
		return Sort.by(
			Sort.Order.desc(NotifyCategoryMongodb.FIELD.METADATA.UPDATE_TIME)
		);
	}

}
