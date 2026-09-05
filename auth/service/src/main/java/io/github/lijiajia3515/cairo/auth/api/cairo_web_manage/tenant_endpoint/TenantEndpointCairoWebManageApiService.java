package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.tenant_endpoint;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.MetadataTenantEndpoint;
import io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint.TenantEndpointConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.CreateTenantEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.DeleteTenantEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.GetTenantEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.ModifyTenantEndpointInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.ModifyTenantEndpointStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_endpoint.CreatedTenantEndpointMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_endpoint.DeletedTenantEndpointMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_endpoint.ModifiedTenantEndpointStatusMessage;
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
 * [cairo_endpoint_user/api] tenant service
 */
@Slf4j
@Validated
@Component
public class TenantEndpointCairoWebManageApiService {

	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;
	private final TenantCommonService tenantCommonService;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final AccountCommonService accountCommonService;

	public TenantEndpointCairoWebManageApiService(RabbitTemplate rabbitTemplate,
													 CairoRabbitmqTool cairoRabbitmqTool,
													 @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
													 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													 TransactionTemplate transactionTemplate,
													 ObjectMapper objectMapper,
													 TenantCommonService tenantCommonService,
													 AppCommonService appCommonService,
													 EndpointCommonService endpointCommonService,
													 AccountCommonService accountCommonService) {
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = objectMapper;
		this.tenantCommonService = tenantCommonService;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.accountCommonService = accountCommonService;
	}


	/**
	 * 获取企业终端列表
	 *
	 * @param args 1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_endpoint:get_tenant_endpoint_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataTenantEndpoint> getTenantEndpointList(@Validated GetTenantEndpointArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(TenantEndpointMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantEndpointMongodb> tas = readMongoTemplate.find(query, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
		return getTenantEndpointList(tas);
	}

	/**
	 * 获取企业应用分页列表
	 *
	 * @return 企业查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_endpoint:get_tenant_endpoint_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataTenantEndpoint> getTenantEndpointPageList(@Validated GetTenantEndpointArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(TenantEndpointMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantEndpointMongodb> tenantEndpointMongodbList = readMongoTemplate.find(query, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
		List<MetadataTenantEndpoint> tenantAppList = getTenantEndpointList(tenantEndpointMongodbList);

		return new Page<>(args, tenantAppList, total);
	}

	/**
	 * 创建企业应用
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_endpoint:create_tenant_endpoint",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createTenantEndpoint(@Validated CreateTenantEndpointArgs args) {
		String currentAccountId = CairoSecurityContextHolder.getSubappAccountId();
		TenantEndpointMongodb insertedTenantEndpointMongodb = transactionTemplate.execute(status -> {
			try {
				TenantEndpointMongodb insertTenantEndpointMongodb = TenantEndpointMongodb.builder()
					.tenantId(args.getTenantId())
					.appId(args.getAppId())
					.endpointId(args.getEndpointId())
					.enabled(true)
					.metadata(AccountMetadataMongodb.builder()
						.createAccountId(currentAccountId)
						.updateAccountId(currentAccountId)
						.build())
					.build();
				return mongoTemplate.insert(insertTenantEndpointMongodb, MongodbConstants.Collection.TENANT_ENDPOINT);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("createTenantEndpoint", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建企业终端失败");
			}
		});

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_ENDPOINT, args.getTenantId(), args.getAppId()),
			objectMapper.writeValueAsString(CreatedTenantEndpointMessage
				.builder()
				.tenantId(args.getTenantId())
				.appId(args.getAppId())
				.endpointId(args.getEndpointId())
				.eventAccountId(currentAccountId)
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
		log.debug("createTenantEndpoint: {}", insertedTenantEndpointMongodb);
	}

	/**
	 * 修改企业应用信息
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_endpoint_info", keys = {"#args.tenantId", "#args.appId", "#args.endpointId"})
	@BizLog(
		bizId = "tenant_endpoint:modify_tenant_endpoint_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyTenantEndpointInfo(@Validated ModifyTenantEndpointInfoArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantEndpointMongodb.FIELD.TENANT_ID).is(args.getTenantId())
					.and(TenantEndpointMongodb.FIELD.APP_ID).is(args.getAppId())
					.and(TenantEndpointMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
				Query query = Query.query(criteria);
				Update update = new Update();


				update.set(TenantEndpointMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantEndpointMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业终端失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantEndpoint", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业终端失败");
			}
		});
	}

	/**
	 * 修改企业应用状态
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_endpoint_status", keys = {"#args.tenantId", "#args.appId", "#args.endpointId"})
	@BizLog(
		bizId = "tenant_endpoint:modify_tenant_endpoint_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyTenantEndpointStatus(@Validated ModifyTenantEndpointStatusArgs args) {
		String currentAccountId = CairoSecurityContextHolder.getSubappAccountId();
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantEndpointMongodb.FIELD.TENANT_ID).is(args.getTenantId())
					.and(TenantEndpointMongodb.FIELD.APP_ID).is(args.getAppId())
					.and(TenantEndpointMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());

				Query query = Query.query(criteria);
				Update update = new Update();
				Optional.ofNullable(args.getEnabled()).ifPresent(x -> update.set(TenantEndpointMongodb.FIELD.ENABLED, args.getEnabled()));
				update.set(TenantEndpointMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantEndpointMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业终端状态失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantEndpointStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业应用状态失败");
			}
		});

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_TENANT_ENDPOINT_STATUS, args.getTenantId(), args.getAppId()),
			objectMapper.writeValueAsString(ModifiedTenantEndpointStatusMessage
				.builder()
				.tenantId(args.getTenantId())
				.appId(args.getAppId())
				.endpointId(args.getEndpointId())
				.eventAccountId(currentAccountId)
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 删除企业终端
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_endpoint", keys = {"#args.tenantId", "#args.appId", "#args.endpointId"})
	@BizLog(
		bizId = "tenant_endpoint:delete_tenant_endpoint",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void deleteTenantEndpoint(@Validated DeleteTenantEndpointArgs args) {
		String currentAccountId = CairoSecurityContextHolder.getSubappAccountId();
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantEndpointMongodb.FIELD.TENANT_ID).is(args.getTenantId())
					.and(TenantEndpointMongodb.FIELD.APP_ID).is(args.getAppId())
					.and(TenantEndpointMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());

				Query query = Query.query(criteria);

				Update update = new Update();
				update.set(TenantEndpointMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantEndpointMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
				if (updateResult.getModifiedCount() <= 0) {
					throw new ConflictBusinessException("删除企业终端失败");
				}

				TenantEndpointMongodb deletedTenantEndpointMongodb = mongoTemplate.findAndRemove(query, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
				if (deletedTenantEndpointMongodb != null) {
					mongoTemplate.insert(deletedTenantEndpointMongodb, MongodbConstants.DeletedCollection.TENANT_ENDPOINT);
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteTenantEndpoint", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除企业终端失败");
			}
		});

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_ENDPOINT, args.getTenantId(), args.getAppId()),
			objectMapper.writeValueAsString(DeletedTenantEndpointMessage
				.builder()
				.tenantId(args.getTenantId())
				.appId(args.getAppId())
				.endpointId(args.getEndpointId())
				.eventAccountId(currentAccountId)
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
	private Criteria buildCriteria(GetTenantEndpointArgs args) {
		Criteria criteria = new Criteria();

		Optional.ofNullable(args.getTenantId()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantEndpointMongodb.FIELD.TENANT_ID).is(x));
		Optional.ofNullable(args.getAppId()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantEndpointMongodb.FIELD.APP_ID).is(x));
		Optional.ofNullable(args.getEndpointId()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantEndpointMongodb.FIELD.ENDPOINT_ID).is(x));
		Optional.ofNullable(args.getEnabled()).ifPresent(x -> criteria.and(TenantEndpointMongodb.FIELD.TENANT_ID).is(x));

		return criteria;
	}

	List<MetadataTenantEndpoint> getTenantEndpointList(List<TenantEndpointMongodb> mongodbList) {
		List<String> tenantIds = mongodbList.stream().map(TenantEndpointMongodb::getTenantId).distinct().collect(Collectors.toList());
		Map<String, Tenant> tenantMap = Optional.of(tenantIds)
			.filter(innerTenantIds -> !innerTenantIds.isEmpty())
			.map(tenantCommonService::getBasicTenantMapByTenantIds)
			.orElse(Collections.emptyMap());

		List<String> appIds = mongodbList.stream().map(TenantEndpointMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());


		List<String> endpointIds = mongodbList.stream().map(TenantEndpointMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = Optional.of(endpointIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(endpointCommonService::getEndpointMapByEndpointIds)
			.orElse(Collections.emptyMap());


		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(mongodbList.stream().map(TenantEndpointMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, Account> metadataAccountMap = accountCommonService.getAccountMapByAccountIds(metadataAccountIds);

		return mongodbList.stream().map(x -> TenantEndpointConverter.convertTenantEndpoint(x, tenantMap, appMap, endpointMap, metadataAccountMap)).collect(Collectors.toList());
	}

}
