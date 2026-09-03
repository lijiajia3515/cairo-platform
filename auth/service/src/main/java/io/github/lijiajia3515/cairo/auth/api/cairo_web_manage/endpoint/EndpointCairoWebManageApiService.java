package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.endpoint;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.scope.AccessScope;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.EndpointType;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.MetadataEndpoint;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.CreateEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.DeleteEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.GetEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.ModifyEndpointInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.ModifyEndpointStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.CreatedEndpointMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.DeletedEndpointMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.ModifiedEndpointInfoMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.ModifiedEndpointStatusMessage;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import io.micrometer.tracing.annotation.NewSpan;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [cairo_endpoint_user/api]endpoint service
 */
@Slf4j
@Validated
@Component
public class EndpointCairoWebManageApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;
	private final AppCommonService appCommonService;
	private final AppUserCommonService appUserCommonService;
	private final FileCommonService fileCommonService;

	public EndpointCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
											   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
											   TransactionTemplate transactionTemplate,
											   RabbitTemplate rabbitTemplate,
											   CairoRabbitmqTool cairoRabbitmqTool,
											   ObjectMapper objectMapper,
											   CairoSecurityProperties cairoSecurityProperties,
											   AppCommonService appCommonService,
											   AppUserCommonService appUserCommonService, FileCommonService fileCommonService) {
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = objectMapper;
		this.appCommonService = appCommonService;
		this.appUserCommonService = appUserCommonService;
		this.fileCommonService = fileCommonService;
	}


	/**
	 * 获取端点集合
	 *
	 * @param args 参数
	 * @return 端点集合
	 */
	@NewSpan
	@BizLog(
		bizId = "endpoint:get_endpoint_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataEndpoint> getEndpointList(@Validated GetEndpointArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(EndpointMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<EndpointMongodb> tms = readMongoTemplate.find(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
		return getEndpointList(tms);
	}

	/**
	 * 端点查询
	 *
	 * @return 端点查询
	 */
	@NewSpan
	@BizLog(
		bizId = "endpoint:get_endpoint_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataEndpoint> getEndpointPageList(@Validated GetEndpointArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(EndpointMongodb.FIELD.METADATA.UPDATE_TIME)));

		long total = readMongoTemplate.count(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(EndpointMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<MetadataEndpoint> ds = getEndpointList(readMongoTemplate.find(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT));

		return new Page<>(args, ds, total);
	}


	/**
	 * 创建应用
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "endpoint:create_endpoint",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createEndpoint(@Validated CreateEndpointArgs args) {
		EndpointType type = EndpointType.typeValueOf(args.getType()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 类型：%s 错误", args.getType())));
		AccessScope scope = AccessScope.scopeValueOf(args.getScope()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 范围：%s 错误", args.getScope())));
		EndpointMongodb endpointMongodb = transactionTemplate.execute(status -> {
			try {
				// appid 不合法就中断
				appCommonService.checkAppId(mongoTemplate, args.getAppId());

				// 范围超出应用允许范围就中断
				appCommonService.checkAppScope(mongoTemplate, args.getAppId(), scope.getScopeValue());

				EndpointMongodb newEndpointMongodb = EndpointMongodb.builder()
					.id(CoreConstants.nextIdStr())
					.appId(args.getAppId())
					.endpointId(args.getEndpointId())
					.endpointName(args.getEndpointName())
					.type(type.getTypeValue())
					.scope(scope.getScopeValue())
					.icon(args.getIcon())
					.websiteUrl(args.getWebsiteUrl())
					.enabled(args.isEnabled())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build())
					.build();

				return mongoTemplate.insert(newEndpointMongodb, MongodbConstants.Collection.ENDPOINT);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("createEndpoint", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建终端失败");
			}
		});

		if (endpointMongodb != null) {
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_ENDPOINT, endpointMongodb.getAppId()),
				objectMapper.writeValueAsString(CreatedEndpointMessage
					.builder()
					.appId(endpointMongodb.getAppId())
					.endpointId(endpointMongodb.getEndpointId())
					.endpointName(endpointMongodb.getEndpointName())
					.type(endpointMongodb.getType())
					.scope(endpointMongodb.getScope())
					.icon(endpointMongodb.getIcon())
					.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
					.eventTime(endpointMongodb.getMetadata().getCreateTime())
					.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}

	}

	/**
	 * 修改端点信息
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "endpoint:modify_endpoint_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_endpoint_info", keys = {"#args.id"})
	public void modifyEndpointInfo(@Validated ModifyEndpointInfoArgs args) {
		// type校验
		EndpointType type;
		if (args.getType() != null) {
			type = EndpointType.typeValueOf(args.getType()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 类型：%s 错误", args.getType())));
		} else {
			type = null;
		}

		// scope校验
		AccessScope scope;
		if (args.getScope() != null) {
			scope = AccessScope.scopeValueOf(args.getScope()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 范围：%s 错误", args.getScope())));
			// 范围超出应用允许范围就中断
			Query endpointQuery = Query.query(Criteria.where(EndpointMongodb.FIELD.ID).is(args.getId()));
			endpointQuery.fields().include(EndpointMongodb.FIELD.APP_ID);
			EndpointMongodb endpoint = mongoTemplate.findOne(endpointQuery, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
			if (endpoint != null) {
				appCommonService.checkAppScope(mongoTemplate, endpoint.getAppId(), scope.getScopeValue());
			}
		} else {
			scope = null;
		}

		EndpointMongodb modifiedEndpointMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(EndpointMongodb.FIELD.ID).is(args.getId())
				);
				Update update = new Update();

				if (args.getEndpointId() != null) {
					update.set(EndpointMongodb.FIELD.ENDPOINT_ID, args.getEndpointId());
				}

				if (args.getEndpointName() != null) {
					update.set(EndpointMongodb.FIELD.ENDPOINT_NAME, args.getEndpointName());
				}

				if (type != null) {
					update.set(EndpointMongodb.FIELD.TYPE, type.getTypeValue());
				}

				if (scope != null) {
					update.set(EndpointMongodb.FIELD.SCOPE, scope.getScopeValue());
				}

				if (args.getIcon() != null) {
					update.set(EndpointMongodb.FIELD.ICON, args.getIcon());
				}

				if (args.getWebsiteUrl() != null) {
					update.set(EndpointMongodb.FIELD.WEBSITE_URL, args.getWebsiteUrl());
				}

				update.currentDate(EndpointMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(EndpointMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyEndpoint", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改终端信息失败");
			}
		});

		if (modifiedEndpointMongodb == null) {
			throw new ConflictBusinessException("修改终端信息失败");
		}

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_ENDPOINT_INFO, modifiedEndpointMongodb.getAppId()),
			objectMapper.writeValueAsString(ModifiedEndpointInfoMessage.builder()
				.id(args.getId())
				.appId(modifiedEndpointMongodb.getAppId())
				.appName(modifiedEndpointMongodb.getAppId())
				.oldEndpointId(modifiedEndpointMongodb.getEndpointId())
				.oldEndpointName(modifiedEndpointMongodb.getEndpointName())
				.newEndpointId(Optional.ofNullable(args.getEndpointId()).orElse(modifiedEndpointMongodb.getEndpointId()))
				.newEndpointName(Optional.ofNullable(args.getEndpointName()).orElse(modifiedEndpointMongodb.getEndpointName()))
				.oldType(modifiedEndpointMongodb.getType())
				.newType(Optional.ofNullable(args.getType()).orElse(modifiedEndpointMongodb.getType()))
				.oldScope(modifiedEndpointMongodb.getScope())
				.newType(Optional.ofNullable(args.getScope()).orElse(modifiedEndpointMongodb.getScope()))
				.icon(Optional.ofNullable(args.getIcon()).orElse(modifiedEndpointMongodb.getIcon()))
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);

	}

	/**
	 * 终端开启/关闭
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "endpoint:modify_endpoint_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_endpoint_status", keys = {"#args.id"})
	public void modifyEndpointStatus(@Validated ModifyEndpointStatusArgs args) {
		EndpointMongodb endpointMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(EndpointMongodb.FIELD.ID).is(args.getId())
				);
				Update update = Update.update(EndpointMongodb.FIELD.ENABLED, args.getEnabled());
				update.currentDate(EndpointMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(EndpointMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				return Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT))
					.orElseThrow(() -> new ConflictBusinessException("修改终端状态失败"));
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyEndpointStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改终端状态失败");
			}
		});
		if (endpointMongodb != null) {
			ModifiedEndpointStatusMessage message = ModifiedEndpointStatusMessage.builder()
				.appId(endpointMongodb.getAppId())
				.endpointId(endpointMongodb.getEndpointId())
				.enabled(endpointMongodb.getEnabled())
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(LocalDateTime.now())
				.build();
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_ENDPOINT_STATUS, message.getAppId()),
				objectMapper.writeValueAsString(message),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}

	}

	/**
	 * 删除终端
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "endpoint:delete_endpoint",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	@Lock4j(name = "delete_endpoint", keys = {"#args.id"})
	public void deleteEndpoint(@Validated DeleteEndpointArgs args) {
		EndpointMongodb deletedEndpointMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria.where(EndpointMongodb.FIELD.ID).is(args.getId()));
				EndpointMongodb endpointMongodb = mongoTemplate.findOne(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
				if (endpointMongodb == null) {
					throw new ConflictBusinessException("删除终端失败(终端不存在)");
				}
				Update update = new Update();
				update.set(EndpointMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(EndpointMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
				log.debug("deleteEndpoint updateResult: {}", updateResult);
				EndpointMongodb deletedEndpoint = mongoTemplate.findAndRemove(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);

				if (deletedEndpoint == null) {
					throw new ConflictBusinessException("删除终端失败");
				}
				EndpointMongodb insert = mongoTemplate.insert(deletedEndpoint, MongodbConstants.DeletedCollection.ENDPOINT);
				return deletedEndpoint;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteEndpoint", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除终端失败");
			}
		});

		if (deletedEndpointMongodb != null) {
			if (deletedEndpointMongodb.getIcon() != null) {
				fileCommonService.deletePublicFile(deletedEndpointMongodb.getAppId().concat("/").concat(FileKeyPrefixConstants.ENDPOINT_ICON_PREFIX), Collections.singletonList(deletedEndpointMongodb.getIcon()));
			}
			DeletedEndpointMessage deletedEndpointMessage = DeletedEndpointMessage.builder()
				.appId(deletedEndpointMongodb.getAppId())
				.endpointId(deletedEndpointMongodb.getEndpointId())
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(LocalDateTime.now())
				.build();
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_ENDPOINT, deletedEndpointMessage.getAppId()),
				objectMapper.writeValueAsString(deletedEndpointMessage),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}

	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetEndpointArgs args) {
		Criteria criteria = new Criteria();

		if (args.getAppId() != null) {
			criteria.and(EndpointMongodb.FIELD.APP_ID).is(args.getAppId());
		}

		if (args.getTypeIds() != null && !args.getTypeIds().isEmpty()) {
			criteria.and(EndpointMongodb.FIELD.TYPE).in(args.getTypeIds());
		}

		if (args.getScopeIds() != null && !args.getScopeIds().isEmpty()) {
			criteria.and(EndpointMongodb.FIELD.SCOPE).in(args.getScopeIds());
		}

		if (args.getEnabled() != null) {
			criteria.and(EndpointMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getKeyword() != null) {
			criteria.and(EndpointMongodb.FIELD.ENDPOINT_NAME).regex(args.getKeyword());
		}

		return criteria;
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return cairo user endpoint list
	 */
	List<MetadataEndpoint> getEndpointList(List<EndpointMongodb> ms) {
		List<String> appIds = ms.stream().map(EndpointMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(EndpointMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds))
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> EndpointConverter.convertMetadataEndpoint(x, appMap, metadataUserMap)).collect(Collectors.toList());
	}
}
