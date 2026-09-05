package io.github.lijiajia3515.cairo.auth.api.subapp.tenant_subapp;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.MetadataTenantSubapp;
import io.github.lijiajia3515.cairo.auth.modules.tenant_subapp.TenantSubappConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_subapp.CreateTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_subapp.DeleteTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_subapp.GetTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_subapp.ModifyTenantSubappStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_subapp.CreatedTenantSubappMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_subapp.DeletedTenantSubappMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_subapp.ModifiedTenantSubappStatusMessage;
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
 * [subapp_user/api] tenant app subapp service
 */
@Slf4j
@Validated
@Component
public class TenantSubappSubappApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final TenantCommonService tenantCommonService;
	private final AppCommonService appCommonService;
	private final AccountCommonService accountCommonService;
	private final EndpointCommonService endpointCommonService;
	private final SubappCommonService subappCommonService;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;

	public TenantSubappSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
												@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												TransactionTemplate transactionTemplate,
												TenantCommonService tenantCommonService,
												AppCommonService appCommonService,
												AccountCommonService accountCommonService,
												EndpointCommonService endpointCommonService,
												SubappCommonService subappCommonService, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.tenantCommonService = tenantCommonService;
		this.appCommonService = appCommonService;
		this.accountCommonService = accountCommonService;
		this.endpointCommonService = endpointCommonService;
		this.subappCommonService = subappCommonService;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
	}


	/**
	 * 获取企业子应用列表
	 *
	 * @param appId appId
	 * @param args  1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_subapp:get_tenant_subapp_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataTenantSubapp> getTenantSubappList(String appId, @Validated GetTenantSubappArgs args) {
		Criteria criteria = buildCriteria(appId,args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(TenantSubappMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantSubappMongodb> tas = readMongoTemplate.find(query, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
		return getTenantSubappList(tas);
	}

	/**
	 * 获取企业子应用分页列表
	 *
	 * @return 企业查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_subapp:get_tenant_subapp_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataTenantSubapp> getTenantSubappPageList(String appId, @Validated GetTenantSubappArgs args) {
		Criteria criteria = buildCriteria(appId,args);
		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, TenantMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(TenantSubappMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantSubappMongodb> tenantAppMongodbList = readMongoTemplate.find(query, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
		List<MetadataTenantSubapp> tenantAppList = getTenantSubappList(tenantAppMongodbList);

		return new Page<>(args, tenantAppList, total);
	}

	/**
	 * 创建企业子应用
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_subapp:create_tenant_subapp",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createTenantSubapp(String appId, @Validated CreateTenantSubappArgs args) {
		String currentAccountId = CairoSecurityContextHolder.getSubappAccountId();
		TenantSubappMongodb insertedTenantSubappMongodb = transactionTemplate.execute(status -> {
			try {
				TenantSubappMongodb insertTenantEndpointMongodb = TenantSubappMongodb.builder()
					.tenantId(args.getTenantId())
					.appId(appId)
					.endpointId(args.getEndpointId())
					.subappId(args.getSubappId())
					.enabled(args.getEnabled())
					.metadata(AccountMetadataMongodb.builder()
						.createAccountId(currentAccountId)
						.updateAccountId(currentAccountId)
						.build())
					.build();
				return mongoTemplate.insert(insertTenantEndpointMongodb, MongodbConstants.Collection.TENANT_SUBAPP);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("createTenantSubapp", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建企业子应用失败");
			}
		});
		log.debug("createTenantSubapp: {}", insertedTenantSubappMongodb);
		//发送企业子应用创建完成消息
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_SUBAPP, args.getTenantId(), appId),
			objectMapper.writeValueAsString(CreatedTenantSubappMessage
				.builder()
				.tenantId(args.getTenantId())
				.appId(appId)
				.endpointId(args.getEndpointId())
				.subappId(args.getSubappId())
				.eventAccountId(currentAccountId)
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 修改企业子应用状态
	 *
	 * @param appId
	 * @param args  参数
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_subapp_status", keys = {"#args.tenantId", "#appId", "#args.endpointId", "#args.subappId"})
	@BizLog(
		bizId = "tenant_subapp:modify_tenant_subapp_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyTenantSubappStatus(String appId, @Validated ModifyTenantSubappStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantSubappMongodb.FIELD.TENANT_ID).is(args.getTenantId())
					.and(TenantSubappMongodb.FIELD.APP_ID).is(appId)
					.and(TenantSubappMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
					.and(TenantSubappMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());

				Query query = Query.query(criteria);
				Update update = new Update();
				Optional.ofNullable(args.getEnabled()).ifPresent(x -> update.set(TenantSubappMongodb.FIELD.ENABLED, args.getEnabled()));
				update.set(TenantSubappMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantSubappMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业子应用状态失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantSubappStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业子应用状态失败");
			}
		});
		//发送企业子应用状态修改完成消息
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_TENANT_SUBAPP_STATUS, args.getTenantId(), appId),
			objectMapper.writeValueAsString(ModifiedTenantSubappStatusMessage
				.builder()
				.tenantId(args.getTenantId())
				.appId(appId)
				.endpointId(args.getEndpointId())
				.subappId(args.getSubappId())
				.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 删除企业子应用
	 *
	 * @param appId
	 * @param args  参数
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_subapp", keys = {"#args.tenantId", "#appId", "#args.endpointId", "#args.subappId"})
	@BizLog(
		bizId = "tenant_subapp:delete_tenant_subapp",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void deleteTenantSubapp(String appId, @Validated DeleteTenantSubappArgs args) {
		TenantSubappMongodb deleted = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantSubappMongodb.FIELD.TENANT_ID).is(args.getTenantId())
					.and(TenantSubappMongodb.FIELD.APP_ID).is(appId)
					.and(TenantSubappMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
					.and(TenantSubappMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
				Query query = Query.query(criteria);

				Update update = new Update();
				update.set(TenantSubappMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantSubappMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
				if (updateResult.getModifiedCount() <= 0) {
					throw new ConflictBusinessException("删除企业子应用失败");
				}
				TenantSubappMongodb deletedTenantMongodb = mongoTemplate.findAndRemove(query, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
				if (deletedTenantMongodb != null) {
					mongoTemplate.insert(deletedTenantMongodb, MongodbConstants.DeletedCollection.TENANT_SUBAPP);
				}
				return deletedTenantMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteTenantSubapp", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除企业子应用");
			}
		});
        //发送企业子应用删除完成消息
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_SUBAPP, args.getTenantId(), appId),
			objectMapper.writeValueAsString(DeletedTenantSubappMessage
				.builder()
				.tenantId(args.getTenantId())
				.appId(appId)
				.endpointId(args.getEndpointId())
				.subappId(args.getSubappId())
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
	private Criteria buildCriteria(String appId,GetTenantSubappArgs args) {
		Criteria criteria = new Criteria();

		Optional.ofNullable(args.getTenantId()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantSubappMongodb.FIELD.TENANT_ID).is(x));
		Optional.ofNullable(appId).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantSubappMongodb.FIELD.APP_ID).is(x));
		Optional.ofNullable(args.getEndpointId()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantSubappMongodb.FIELD.ENDPOINT_ID).is(x));
		Optional.ofNullable(args.getEnabled()).ifPresent(x -> criteria.and(TenantSubappMongodb.FIELD.ENABLED).is(x));
		Optional.ofNullable(args.getSubappId()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantSubappMongodb.FIELD.SUBAPP_ID).is(x));

		return criteria;
	}

	List<MetadataTenantSubapp> getTenantSubappList(List<TenantSubappMongodb> tas) {
		List<String> tenantIds = tas.stream().map(TenantSubappMongodb::getTenantId).distinct().collect(Collectors.toList());
		Map<String, Tenant> tenantMap = Optional.of(tenantIds)
			.filter(innerTenantIds -> !innerTenantIds.isEmpty())
			.map(tenantCommonService::getBasicTenantMapByTenantIds)
			.orElse(Collections.emptyMap());

		List<String> appIds = tas.stream().map(TenantSubappMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());


		List<String> endpointIds = tas.stream().map(TenantSubappMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = Optional.of(endpointIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(endpointCommonService::getEndpointMapByEndpointIds)
			.orElse(Collections.emptyMap());


		List<String> subappIds = tas.stream().map(TenantSubappMongodb::getSubappId).distinct().collect(Collectors.toList());
		Map<String, Subapp> subappMap = Optional.of(subappIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(subappCommonService::getSubappMapBySubappIds)
			.orElse(Collections.emptyMap());


		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(tas.stream().map(TenantSubappMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, Account> metadataAccountMap = accountCommonService.getAccountMapByAccountIds(metadataAccountIds);

		return tas.stream().map(x -> TenantSubappConverter.convertTenantSubapp(x, tenantMap, appMap, endpointMap, subappMap, metadataAccountMap)).collect(Collectors.toList());
	}

}
