package io.github.lijiajia3515.cairo.auth.api.subapp.tenant_app;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.MetadataTenantApp;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app.TenantAppConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.CreateTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.DeleteTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.GetTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.ModifyTenantAppInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.ModifyTenantAppStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.CreatedTenantAppMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.DeletedTenantAppMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.ModifiedTenantAppInfoMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.ModifiedTenantAppStatusMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_endpoint.CreatedTenantEndpointMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.micrometer.tracing.annotation.NewSpan;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [subapp_user/api] tenant app service
 */
@Slf4j
@Validated
@Component
public class TenantAppSubappApiService {

	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;
	private final TenantCommonService tenantCommonService;
	private final AppCommonService appCommonService;
	private final AccountCommonService accountCommonService;

	public TenantAppSubappApiService(RabbitTemplate rabbitTemplate,
										 CairoRabbitmqTool cairoRabbitmqTool,
										 @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										 TransactionTemplate transactionTemplate,
										 ObjectMapper objectMapper,
										 TenantCommonService tenantCommonService,
										 AppCommonService appCommonService,
										 AccountCommonService accountCommonService) {
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = objectMapper;
		this.tenantCommonService = tenantCommonService;
		this.appCommonService = appCommonService;
		this.accountCommonService = accountCommonService;
	}


	/**
	 * 获取企业应用列表
	 *
	 * @param appId
	 * @param args  1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app:get_tenant_app_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataTenantApp> getTenantAppList(String appId, @Validated GetTenantAppArgs args) {
		Criteria criteria = buildCriteria(appId,args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(TenantAppMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantAppMongodb> tas = readMongoTemplate.find(query, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
		return getTenantAppList(tas);
	}

	/**
	 * 获取企业应用分页列表
	 *
	 * @return 企业查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app:get_tenant_app_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataTenantApp> getTenantAppPageList(String appId, @Validated GetTenantAppArgs args) {
		Criteria criteria = buildCriteria(appId,args);
		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, TenantMongodb.class, MongodbConstants.Collection.TENANT_APP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(TenantAppMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantAppMongodb> tenantAppMongodbList = readMongoTemplate.find(query, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
		List<MetadataTenantApp> tenantAppList = getTenantAppList(tenantAppMongodbList);

		return new Page<>(args, tenantAppList, total);
	}

	/**
	 * 创建企业应用
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_app:create_tenant_app",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createTenantApp(String appId, @Validated CreateTenantAppArgs args) {
		List<String> endpointIds = Optional.ofNullable(args.getEndpointIds()).orElse(Collections.emptyList());
		String currentAccountId = CairoSecurityContextHolder.getSubappAccountId();
		TenantAppMongodb insertedTenantAppMongodb = transactionTemplate.execute(status -> {
			try {
				TenantAppMongodb tenantAppMongodb = TenantAppMongodb.builder()
					.tenantId(args.getTenantId())
					.appId(appId)
					.adminAccountIds(Optional.ofNullable(args.getAdminAccountIds()).filter(x -> !x.isEmpty()).orElse(Collections.singletonList(CairoSecurityContextHolder.getSubappAccountId())))
					.autoRegister(args.isAutoRegister())
					.enabled(args.getEnabled())
					.metadata(AccountMetadataMongodb.builder()
						.createAccountId(currentAccountId)
						.updateAccountId(currentAccountId)
						.build())
					.build();
				tenantAppMongodb = mongoTemplate.insert(tenantAppMongodb, MongodbConstants.Collection.TENANT_APP);
				List<TenantEndpointMongodb> tenantEndpointMongodbList = endpointIds.stream()
					.map(endpointId -> TenantEndpointMongodb.builder()
						.tenantId(args.getTenantId())
						.appId(appId)
						.endpointId(endpointId)
						.enabled(true)
						.metadata(AccountMetadataMongodb.builder()
							.createAccountId(currentAccountId)
							.updateAccountId(currentAccountId)
							.build())
						.build())
					.collect(Collectors.toList());
				if (!tenantEndpointMongodbList.isEmpty()) {
					mongoTemplate.insert(tenantEndpointMongodbList, MongodbConstants.Collection.TENANT_ENDPOINT);
				}
				return tenantAppMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("createTenantApp", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建企业应用失败");
			}
		});

		if (insertedTenantAppMongodb != null) {
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_APP, args.getTenantId(), appId),
				objectMapper.writeValueAsString(CreatedTenantAppMessage
					.builder()
					.tenantId(insertedTenantAppMongodb.getTenantId())
					.appId(insertedTenantAppMongodb.getAppId())
					.enabled(insertedTenantAppMongodb.getEnabled())
					.endpointIds(args.getEndpointIds())
					.subappIds(args.getSubappIds())
					.adminAccountIds(insertedTenantAppMongodb.getAdminAccountIds())
					.eventAccountId(currentAccountId)
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
			endpointIds.forEach(endpointId -> {
				try {
					rabbitTemplate.convertAndSend(
						cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
						cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_ENDPOINT, args.getTenantId(), appId),
						objectMapper.writeValueAsString(CreatedTenantEndpointMessage
							.builder()
							.tenantId(insertedTenantAppMongodb.getTenantId())
							.appId(insertedTenantAppMongodb.getAppId())
							.enabled(insertedTenantAppMongodb.getEnabled())
							.endpointId(endpointId)
							.eventAccountId(currentAccountId)
							.eventTime(LocalDateTime.now())
							.build()
						),
						new CorrelationData(CoreConstants.nextIdStr())
					);
				} catch (JsonProcessingException e) {
					log.warn("e", e);
				}
			});
		}

	}

	/**
	 * 修改企业应用信息
	 *
	 * @param appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_info", keys = {"#appId", "#args.tenantId"})
	@BizLog(
		bizId = "tenant_app:modify_tenant_app_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyTenantAppInfo(String appId, @Validated ModifyTenantAppInfoArgs args) {
		TenantAppMongodb oldTenantApp = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppMongodb.FIELD.TENANT_ID).is(args.getTenantId())
					.and(TenantAppMongodb.FIELD.APP_ID).is(appId);
				Query query = Query.query(criteria);
				Update update = new Update();

				Optional.ofNullable(args.getAutoRegister()).ifPresent(x -> update.set(TenantAppMongodb.FIELD.AUTO_REGISTER, x));
				Optional.ofNullable(args.getAdminAccountIds()).ifPresent(x -> update.set(TenantAppMongodb.FIELD.ADMIN_ACCOUNT_IDS, x));

				TenantAppMongodb oldTenantAppMongodb = mongoTemplate.findOne(query, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);

				update.set(TenantAppMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantAppMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业应用失败");
				}
				return oldTenantAppMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantAppInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业应用信息失败");
			}
		});
		List<String> oldAllAdminAccountIds = Optional.ofNullable(oldTenantApp).map(TenantAppMongodb::getAdminAccountIds).orElse(Collections.emptyList());
		List<String> newAllAdminAccountIds = Optional.ofNullable(args.getAdminAccountIds()).orElse(Collections.emptyList());

		List<String> removeAdminAccountIds = oldAllAdminAccountIds.stream().filter(x -> !newAllAdminAccountIds.contains(x)).collect(Collectors.toList());
		List<String> newAdminAccountIds = newAllAdminAccountIds.stream().filter(x -> !oldAllAdminAccountIds.contains(x)).collect(Collectors.toList());

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_TENANT_APP_INFO, args.getTenantId(), appId),
			objectMapper.writeValueAsString(ModifiedTenantAppInfoMessage
				.builder()
				.tenantId(args.getTenantId())
				.appId(appId)
				.removeAdminAccountIds(removeAdminAccountIds)
				.newAdminAccountIds(newAdminAccountIds)
				.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 修改企业应用状态
	 *
	 * @param appId
	 * @param args  参数
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_status", keys = {"#appId", "#args.tenantId"})
	@BizLog(
		bizId = "tenant_app:modify_tenant_app_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyTenantAppStatus(String appId, @Validated ModifyTenantAppStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppMongodb.FIELD.TENANT_ID).is(args.getTenantId())
					.and(TenantAppMongodb.FIELD.APP_ID).is(appId);

				Query query = Query.query(criteria);
				Update update = new Update();
				Optional.ofNullable(args.getEnabled()).ifPresent(x -> update.set(TenantAppMongodb.FIELD.ENABLED, args.getEnabled()));
				update.set(TenantAppMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantAppMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业应用状态失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantAppStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业应用状态失败");
			}
		});

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_TENANT_APP_STATUS, args.getTenantId(), appId),
			objectMapper.writeValueAsString(ModifiedTenantAppStatusMessage
				.builder()
				.tenantId(args.getTenantId())
				.appId(appId)
				.enabled(args.getEnabled())
				.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 删除企业应用
	 *
	 * @param appId
	 * @param args  参数
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_app", keys = {"#appId", "#args.tenantId"})
	@BizLog(
		bizId = "tenant_app:delete_tenant_app",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void deleteTenantApp(String appId, @Validated DeleteTenantAppArgs args) {
		TenantAppMongodb deletedTenantApp = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppMongodb.FIELD.TENANT_ID).is(args.getTenantId())
					.and(TenantAppMongodb.FIELD.APP_ID).is(appId);
				Query query = Query.query(criteria);

				Update update = new Update();
				update.set(TenantAppMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantAppMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
				if (updateResult.getModifiedCount() <= 0) {
					throw new ConflictBusinessException("删除企业应用失败");
				}
				TenantAppMongodb deletedTenantMongodb = mongoTemplate.findAndRemove(query, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
				if (deletedTenantMongodb != null) {
					mongoTemplate.insert(deletedTenantMongodb, MongodbConstants.DeletedCollection.TENANT_APP);
				}
				return deletedTenantMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteTenantApp", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除企业应用");
			}
		});

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_APP, args.getTenantId(),appId),
			objectMapper.writeValueAsString(DeletedTenantAppMessage
				.builder()
				.tenantId(args.getTenantId())
				.appId(appId)
				.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}


	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(String appId,GetTenantAppArgs args) {
		Criteria criteria = new Criteria();

		Optional.ofNullable(args.getTenantId()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantAppMongodb.FIELD.TENANT_ID).is(x));
		Optional.ofNullable(appId).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantAppMongodb.FIELD.APP_ID).is(x));
		Optional.ofNullable(args.getEnabled()).ifPresent(x -> criteria.and(TenantAppMongodb.FIELD.ENABLED).is(x));

		return criteria;
	}

	List<MetadataTenantApp> getTenantAppList(List<TenantAppMongodb> tas) {
		List<String> appIds = tas.stream().map(TenantAppMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		List<String> tenantIds = tas.stream().map(TenantAppMongodb::getTenantId).distinct().collect(Collectors.toList());
		Map<String, Tenant> tenantMap = Optional.of(tenantIds)
			.filter(innerTenantIds -> !innerTenantIds.isEmpty())
			.map(tenantCommonService::getBasicTenantMapByTenantIds)
			.orElse(Collections.emptyMap());


		List<String> allAdminAccountIds = tas.stream().flatMap(x -> Optional.ofNullable(x.getAdminAccountIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toList());
		Map<String, Account> adminAccountMap = accountCommonService.getAccountMapByAccountIds(allAdminAccountIds);

		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(tas.stream().map(TenantAppMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, Account> metadataAccountMap = accountCommonService.getAccountMapByAccountIds(metadataAccountIds);

		return tas.stream().map(x -> TenantAppConverter.convertTenantApp(x, tenantMap, appMap, adminAccountMap, metadataAccountMap)).collect(Collectors.toList());
	}

}
