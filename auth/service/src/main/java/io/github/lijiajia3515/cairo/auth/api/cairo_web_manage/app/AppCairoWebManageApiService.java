package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.app;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app.AppConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.MetadataApp;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.CreateAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.DeleteAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.ModifyAppInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.ModifyAppStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.app.CreatedAppMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app.DeletedAppMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app.ModifiedAppInfoMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app.ModifiedAppStatusMessage;
import io.github.lijiajia3515.cairo.auth.domain.dto.scope.AccessScope;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.file.FileCommonService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [cairo_endpoint_user/api] app service
 */
@Slf4j
@Validated
@Component
public class AppCairoWebManageApiService {
	private final CairoSecurityProperties cairoSecurityProperties;

	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final MongoTemplate mongoTemplate;

	private final MongoTemplate readMongoTemplate;

	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;

	private final AccountCommonService accountCommonService;
	private final AppUserCommonService appUserCommonService;
	private final FileCommonService fileCommonService;

	public AppCairoWebManageApiService(CairoSecurityProperties cairoSecurityProperties,
									   RabbitTemplate rabbitTemplate,
									   CairoRabbitmqTool cairoRabbitmqTool,
									   @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
									   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
									   TransactionTemplate transactionTemplate,
									   ObjectMapper objectMapper,
									   AccountCommonService accountCommonService,
									   AppUserCommonService appUserCommonService,
									   FileCommonService fileCommonService) {
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = objectMapper;
		this.accountCommonService = accountCommonService;
		this.appUserCommonService = appUserCommonService;
		this.fileCommonService = fileCommonService;
	}


	/**
	 * 获取应用列表
	 *
	 * @param args 1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "app:get_app_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataApp> getAppList(@Validated GetAppArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(
				Sort.Order.desc(AppMongodb.FIELD.METADATA.UPDATE_TIME)
			));

		List<AppMongodb> tms = readMongoTemplate.find(query, AppMongodb.class, MongodbConstants.Collection.APP);
		return getAppList(tms);
	}

	/**
	 * 查找
	 *
	 * @return 租户查询
	 */
	@NewSpan
	@BizLog(
		bizId = "app:get_app_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataApp> getAppPageList(@Validated GetAppArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(AppMongodb.FIELD.METADATA.UPDATE_TIME)));

		long total = readMongoTemplate.count(query, AppMongodb.class, MongodbConstants.Collection.APP);

		query.with(args.pageable());
		query.with(Sort.by(
			Sort.Order.desc(AppMongodb.FIELD.METADATA.UPDATE_TIME)
		));
		List<MetadataApp> ds = getAppList(readMongoTemplate.find(query, AppMongodb.class, MongodbConstants.Collection.APP));

		return new Page<>(args, ds, total);
	}

	/**
	 * 创建应用
	 */
	@NewSpan
	@BizLog(
		bizId = "app:create_app",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void createApp(@Validated CreateAppArgs args) {
		// scopes校验（缺省 [app,tenant]：默认允许平台管理后台与企业级资源，开放需显式加）
		List<AccessScope> scopes = Optional.ofNullable(args.getScopes()).orElse(Collections.emptyList()).stream()
			.map(x -> AccessScope.scopeValueOf(x).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 范围：%s 错误", x))))
			.toList();
		List<String> scopeValues = scopes.isEmpty()
			? List.of(AccessScope.APP.getScopeValue(), AccessScope.TENANT.getScopeValue())
			: args.getScopes();
		AppMongodb appMongodb = transactionTemplate.execute(status -> {
			try {
				AppMongodb app = AppMongodb.builder()
					.appId(args.getAppId())
					.appName(args.getAppName())
					.scopes(scopeValues)
					.privateApp(Optional.ofNullable(args.getPrivateApp()).orElse(false))
					.icon(args.getIcon())
					.enabled(args.isEnabled())
					.adminAccountIds(Optional.ofNullable(args.getAdminAccountIds()).filter(x -> !x.isEmpty()).orElse(Collections.singletonList(CairoSecurityContextHolder.getSubappAccountId())))
					.autoRegister(args.isAutoRegister())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build())
					.build();

				return mongoTemplate.insert(app, MongodbConstants.Collection.APP);
			} catch (Exception e) {
				log.debug("createApp", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建应用失败");
			}
		});

		if (appMongodb == null) {
			throw new ConflictBusinessException("创建应用失败");
		}

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.CREATED_APP),
			objectMapper.writeValueAsString(CreatedAppMessage
				.builder()
				.appId(appMongodb.getAppId())
				.appName(appMongodb.getAppName())
				.icon(appMongodb.getIcon())
				.scopes(appMongodb.getScopes())
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(appMongodb.getMetadata().getCreateTime())
				.build()
			),
			new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
		);
	}

	/**
	 * 修改应用
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_app_info", keys = {"#args.appId"})
	@BizLog(
		bizId = "app:modify_app_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyAppInfo(@Validated ModifyAppInfoArgs args) {
		List<AccessScope> scopes = Optional.ofNullable(args.getScopes()).orElse(Collections.emptyList()).stream()
			.map(x -> AccessScope.scopeValueOf(x).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 范围：%s 错误", x))))
			.toList();
		List<String> oldAllAdminAccountIds = new ArrayList<>();
		List<String> newAllAdminAccountIds = Optional.ofNullable(args.getAdminAccountIds()).orElse(Collections.emptyList());

		AppMongodb modifedAppMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(AppMongodb.FIELD.APP_ID).is(args.getAppId())
				);
				Update update = new Update();

				if (args.getAppName() != null) {
					update.set(AppMongodb.FIELD.APP_NAME, args.getAppName());
				}

				if (args.getIcon() != null) {
					update.set(AppMongodb.FIELD.ICON, args.getIcon());
				}

				if (args.getScopes() != null) {
					update.set(AppMongodb.FIELD.SCOPES, args.getScopes());
				}

				if (args.getPrivateApp() != null) {
					update.set(AppMongodb.FIELD.PRIVATE_APP, args.getPrivateApp());
				}

				if (args.getAutoRegister() != null) {
					update.set(AppMongodb.FIELD.AUTO_REGISTER, args.getAutoRegister());
				}

				if (args.getAdminAccountIds() != null && !args.getAdminAccountIds().isEmpty()) {
					update.set(AppMongodb.FIELD.ADMIN_ACCOUNT_IDS, args.getAdminAccountIds());
				}

				update.currentDate(AppMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(AppMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				AppMongodb old = mongoTemplate.findOne(query, AppMongodb.class, MongodbConstants.Collection.APP);
				if (old != null && old.getAdminAccountIds() != null && !old.getAdminAccountIds().isEmpty()) {
					oldAllAdminAccountIds.addAll(old.getAdminAccountIds());
				}

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(query, update, options, AppMongodb.class, MongodbConstants.Collection.APP);
			} catch (Exception e) {
				log.info("modifyApp", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改应用信息失败");
			}
		});

		if (modifedAppMongodb == null) {
			throw new ConflictBusinessException("修改应用信息失败");
		}

		List<String> removeAdminAccountIds = oldAllAdminAccountIds.stream().filter(x -> !newAllAdminAccountIds.contains(x)).collect(Collectors.toList());
		List<String> newAdminAccountIds = newAllAdminAccountIds.stream().filter(x -> !oldAllAdminAccountIds.contains(x)).collect(Collectors.toList());

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_APP_INFO, modifedAppMongodb.getAppId()),
			objectMapper.writeValueAsString(ModifiedAppInfoMessage
				.builder()
				.appId(modifedAppMongodb.getAppId())
				.appName(modifedAppMongodb.getAppName())
				.privateApp(modifedAppMongodb.getPrivateApp())
				.icon(modifedAppMongodb.getIcon())
				.removeAdminAccountIds(removeAdminAccountIds)
				.newAdminAccountIds(newAdminAccountIds)
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(modifedAppMongodb.getMetadata().getUpdateTime())
				.build()
			),
			new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
		);
	}

	/**
	 * 修改应用状态
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_app_status", keys = {"#args.appId"})
	@BizLog(
		bizId = "app:modify_app_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyAppStatus(@Validated ModifyAppStatusArgs args) {
		AppMongodb appMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(AppMongodb.FIELD.APP_ID).is(args.getAppId())
				);
				Update update = Update.update(AppMongodb.FIELD.ENABLED, args.getEnabled());

				update.set(AppMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppMongodb.FIELD.METADATA.UPDATE_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(query, update, options, AppMongodb.class, MongodbConstants.Collection.APP);
			} catch (Exception e) {
				log.debug("modifyAppStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改应用状态失败");
			}
		});

		if (appMongodb == null) {
			throw new ConflictBusinessException("修改应用状态失败");
		}
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_APP_STATUS, appMongodb.getAppId()),
			objectMapper.writeValueAsString(ModifiedAppStatusMessage
				.builder()
				.appId(appMongodb.getAppId())
				.enabled(args.getEnabled())
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(appMongodb.getMetadata().getUpdateTime())
				.build()
			),
			new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
		);

	}

	/**
	 * 删除应用
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "delete_app", keys = {"#args.appId"})
	@BizLog(
		bizId = "app:delete_app",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void deleteApp(@Validated DeleteAppArgs args) {
		AppMongodb insert = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(AppMongodb.FIELD.APP_ID).is(args.getAppId())
				);
				Update update = new Update();
				update.set(AppMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AppMongodb.class, MongodbConstants.Collection.APP);
				log.debug("deleteApp UpdateResult: {}", updateResult);
				AppMongodb deleteApp = mongoTemplate.findAndRemove(query, AppMongodb.class, MongodbConstants.Collection.APP);
				if (deleteApp == null) {
					throw new ConflictBusinessException("删除应用失败，应用不存在");
				}
				return mongoTemplate.insert(deleteApp, MongodbConstants.DeletedCollection.APP);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除应用失败");
			} catch (Exception e) {
				log.debug("delete", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除应用失败");
			}
		});
		if (insert != null && !StringUtils.isBlank(insert.getIcon())) {
			fileCommonService.deletePublicFile(FileKeyPrefixConstants.APP_ICON_PREFIX, Collections.singletonList(insert.getIcon()));
		}
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_APP, args.getAppId()),
			objectMapper.writeValueAsString(DeletedAppMessage
				.builder()
				.appId(args.getAppId())
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
		);

	}


	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetAppArgs args) {
		Criteria criteria = new Criteria();

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.and(AppMongodb.FIELD.APP_NAME).regex(args.getKeyword());
		}

		if (args.getScopes() != null && !args.getScopes().isEmpty()) {
			criteria.and(AppMongodb.FIELD.SCOPES).in(args.getScopes());
		}

		if (args.getEnabled() != null ) {
			criteria.and(AppMongodb.FIELD.ENABLED).is(args.getEnabled());
		}
		return criteria;
	}

	List<MetadataApp> getAppList(List<AppMongodb> ms) {
		List<String> allAdminAccountIds = ms.stream().flatMap(x -> Optional.ofNullable(x.getAdminAccountIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toList());
		Map<String, Account> adminAccountMap = accountCommonService.getAccountMapByAccountIds(allAdminAccountIds);

		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(AppMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds))
			.orElse(Collections.emptyMap());


		return ms.stream().map(x -> AppConverter.convertApp(x, adminAccountMap, metadataUserMap)).collect(Collectors.toList());
	}

}
