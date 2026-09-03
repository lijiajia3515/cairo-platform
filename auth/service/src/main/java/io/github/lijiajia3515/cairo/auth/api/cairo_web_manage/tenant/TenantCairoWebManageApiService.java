package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.tenant;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.MetadataTenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.CreateTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.DeleteTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.GetTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.ModifyTenantInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.ModifyTenantOwnerArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.ModifyTenantStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant.CreatedTenantMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant.ModifiedTenantInfoMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant.ModifiedTenantStatusMessage;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [cairo-web-manage/api] tenant service
 */
@Slf4j
@Validated
@Component
public class TenantCairoWebManageApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;
	private final AccountCommonService accountCommonService;

	public TenantCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										  TransactionTemplate transactionTemplate,
										  RabbitTemplate rabbitTemplate,
										  CairoRabbitmqTool cairoRabbitmqTool,
										  ObjectMapper objectMapper,
										  AccountCommonService accountCommonService) {
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = objectMapper;
		this.accountCommonService = accountCommonService;
	}


	/**
	 * 租户查询
	 *
	 * @param args 1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant:get_tenant_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataTenant> getTenantList(@Validated GetTenantArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(TenantMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantMongodb> tms = readMongoTemplate.find(query, TenantMongodb.class, MongodbConstants.Collection.TENANT);
		return getTenantList(tms);
	}

	/**
	 * 查找
	 *
	 * @return 租户查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant:get_tenant_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataTenant> getTenantPageList(@Validated GetTenantArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(TenantMongodb.FIELD.METADATA.UPDATE_TIME)));

		long total = readMongoTemplate.count(query, TenantMongodb.class, MongodbConstants.Collection.TENANT);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(TenantMongodb.FIELD.TENANT_ID),
				Sort.Order.asc(TenantMongodb.FIELD._ID)
			)
		);
		List<MetadataTenant> ds = getTenantList(readMongoTemplate.find(query, TenantMongodb.class, MongodbConstants.Collection.TENANT));

		return new Page<>(args, ds, total);
	}

	/**
	 * 租户 - 保存
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant:create_tenant",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createTenant(@Validated CreateTenantArgs args) {
		TenantMongodb tenantMongodb = transactionTemplate.execute(status -> {
			try {
				TenantMongodb mongo = TenantMongodb.builder()
					.tenantId(Optional.ofNullable(args.getTenantId()).filter(x -> !x.isBlank()).orElse(TenantCommonService.getNewTenantId()))
					.tenantName(args.getTenantName())
					.aliasName(args.getAliasName())
					.ownerAccountId(Optional.ofNullable(args.getOwnerAccountId()).orElse(CairoSecurityContextHolder.getSubappAccountId()))
					.icon(args.getIcon())
					.enabled(args.isEnabled())
					.metadata(AccountMetadataMongodb.builder()
						.createAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.updateAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.build()
					)
					.build();

				return mongoTemplate.insert(mongo, MongodbConstants.Collection.TENANT);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("createTenant", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建企业失败");
			}
		});

		if (tenantMongodb == null) {
			throw new ConflictBusinessException("创建企业失败");
		}

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT, tenantMongodb.getTenantId()),
			objectMapper.writeValueAsString(CreatedTenantMessage
				.builder()
				.tenantId(tenantMongodb.getTenantId())
				.tenantName(tenantMongodb.getTenantName())
				.aliasName(tenantMongodb.getAliasName())
				.ownerAccountId(tenantMongodb.getOwnerAccountId())
				.icon(tenantMongodb.getIcon())
				.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 修改企业信息
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_info", keys = {"#args.tenantId"})
	@BizLog(
		bizId = "tenant:modify_tenant_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyTenantInfo(@Validated ModifyTenantInfoArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantMongodb.FIELD.TENANT_ID).is(args.getTenantId()));
				Update update = new Update();
				if (args.getTenantName() != null) {
					update.set(TenantMongodb.FIELD.TENANT_NAME, args.getTenantName());
				}

				if (args.getAliasName() != null) {
					update.set(TenantMongodb.FIELD.ALIAS_NAME, args.getAliasName());
				}

				if (args.getIcon() != null) {
					update.set(TenantMongodb.FIELD.ICON, args.getIcon());
				}

				update.set(TenantMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantMongodb.class, MongodbConstants.Collection.TENANT);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业信息失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业信息失败");
			}
		});

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantKey(CairoAuthRabbitmqRouteKey.MODIFIED_TENANT_INFO, args.getTenantId()),
			objectMapper.writeValueAsString(ModifiedTenantInfoMessage
				.builder()
				.tenantId(args.getTenantId())
				.tenantName(args.getTenantName())
				.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);

	}

	/**
	 * 修改企业拥有着
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_owner", keys = {"#args.tenantId"})
	@BizLog(
		bizId = "tenant:modify_tenant_owner",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyTenantOwner(@Validated ModifyTenantOwnerArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantMongodb.FIELD.TENANT_ID).is(args.getTenantId())
				);
				Update update = new Update();
				if (args.getOwnerAccountId() != null) {
					update.set(TenantMongodb.FIELD.OWNER_ACCOUNT_ID, args.getOwnerAccountId());
				}
				update.set(TenantMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantMongodb.class, MongodbConstants.Collection.TENANT);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业拥有者失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantOwner", e);
				throw new ConflictBusinessException("修改企业拥有者失败");
			}
		});
	}


	/**
	 * 修改企业状态
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_status", keys = {"#args.tenantId"})
	@BizLog(
		bizId = "tenant:modify_tenant_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyTenantStatus(@Validated ModifyTenantStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantMongodb.FIELD.TENANT_ID).is(args.getTenantId())
				);
				Update update = Update.update(TenantMongodb.FIELD.ENABLED, args.getEnabled());
				update.set(TenantMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(TenantMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantMongodb.class, MongodbConstants.Collection.TENANT);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业状态失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业状态失败");
			}
		});

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantKey(CairoAuthRabbitmqRouteKey.MODIFIED_TENANT_STATUS, args.getTenantId()),
			objectMapper.writeValueAsString(ModifiedTenantStatusMessage
				.builder()
				.tenantId(args.getTenantId())
				.enabled(args.getEnabled())
				.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 删除企业
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant", keys = {"#args.tenantId"})
	@BizLog(
		bizId = "tenant:delete_tenant",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void deleteTenant(@Validated DeleteTenantArgs args) {
		throw new ConflictBusinessException("暂不实现");
	}


	/**
	 * 构建查询条件
	 *
	 * @param param 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetTenantArgs param) {
		Criteria criteria = new Criteria();
		Optional.ofNullable(param.getKeyword()).filter(keyword -> !keyword.isBlank()).ifPresent(name -> criteria.and(TenantMongodb.FIELD.TENANT_NAME).regex(name));
		Optional.ofNullable(param.getEnabled()).ifPresent(enabled -> criteria.and(TenantMongodb.FIELD.ENABLED).is(enabled));
		return criteria;
	}

	List<MetadataTenant> getTenantList(List<TenantMongodb> ms) {

		List<String> ownerAccountIds = ms.stream().map(TenantMongodb::getOwnerAccountId).filter(Objects::nonNull).collect(Collectors.toList());
		Map<String, Account> accountMap = accountCommonService.getAccountMapByAccountIds(ownerAccountIds);

		List<AccountMetadataMongodb> metadataMongodbList = ms.stream().map(TenantMongodb::getMetadata).collect(Collectors.toList());
		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(metadataMongodbList);

		Map<String, Account> metadataAccountMap = accountCommonService.getAccountMapByAccountIds(metadataAccountIds);

		return ms.stream().map(x -> TenantConverter.convertMetadataTenant(x, accountMap, metadataAccountMap)).collect(Collectors.toList());
	}

}
