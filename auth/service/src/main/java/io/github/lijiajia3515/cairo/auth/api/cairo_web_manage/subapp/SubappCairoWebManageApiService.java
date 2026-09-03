package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.subapp;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.file.FileCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.MetadataSubapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.scope.AccessScope;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.CreateSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.DeleteSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.GetSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.ModifySubappInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.ModifySubappStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.MoveSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.CreatedSubappMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.DeletedSubappMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.ModifiedSubappInfoMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.ModifiedSubappStatusMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [cairo-web-manage/api] subapp service
 */
@Slf4j
@Validated
@Component
public class SubappCairoWebManageApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final AppUserCommonService appUserCommonService;
	private final FileCommonService fileCommonService;

	public SubappCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										   TransactionTemplate transactionTemplate,
										   RabbitTemplate rabbitTemplate,
										   CairoRabbitmqTool cairoRabbitmqTool,
										   ObjectMapper objectMapper,
										   CairoSecurityProperties cairoSecurityProperties,
										   AppCommonService appCommonService, EndpointCommonService endpointCommonService,
										   AppUserCommonService appUserCommonService, FileCommonService fileCommonService) {
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = objectMapper;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
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
		bizId = "subapp:get_subapp_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataSubapp> getSubappList(@Valid @NotNull String appId, String endpointId, @Validated GetSubappArgs args) {
		Criteria criteria = buildCriteria(appId, endpointId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(SubappMongodb.FIELD.APP_ID),
					Sort.Order.asc(SubappMongodb.FIELD.ENDPOINT_ID),
					Sort.Order.asc(SubappMongodb.FIELD.SORT)
				)
			);

		List<SubappMongodb> tms = readMongoTemplate.find(query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
		return getSubappList(tms);
	}

	/**
	 * 端点查询
	 *
	 * @return 端点查询
	 */
	@NewSpan
	@BizLog(
		bizId = "subapp:get_subapp_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataSubapp> getSubappPageList(@Valid @NotNull String appId, String endpointId, @Validated GetSubappArgs args) {
		Criteria criteria = buildCriteria(appId, endpointId, args);
		Query query = Query.query(criteria);
		long total = readMongoTemplate.count(query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(SubappMongodb.FIELD.APP_ID),
				Sort.Order.asc(SubappMongodb.FIELD.ENDPOINT_ID),
				Sort.Order.asc(SubappMongodb.FIELD.SORT)
			)
		);

		List<MetadataSubapp> ds = getSubappList(readMongoTemplate.find(query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP));

		return new Page<>(args, ds, total);
	}


	/**
	 * 创建子应用
	 *
	 * @param appId         应用ID
	 * @param endpointId 终端ID
	 * @param args          参数
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "subapp:create_subapp",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createSubapp(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Validated CreateSubappArgs args) {

		// scope校验（缺省 public：随终端开通自动可用）
		AccessScope scope;
		if (args.getScope() != null) {
			scope = AccessScope.scopeValueOf(args.getScope()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 范围：%s 错误", args.getScope())));
		} else {
			scope = AccessScope.PUBLIC;
		}
		SubappMongodb subappMongodb = transactionTemplate.execute(status -> {
			try {
				// endpointId 不合法就中断
				endpointCommonService.checkEndpointId(mongoTemplate, appId, endpointId);

				// 范围超出应用允许范围就中断
				appCommonService.checkAppScope(mongoTemplate, appId, scope.getScopeValue());

				SubappMongodb newSubappMongodb = SubappMongodb.builder()
					.id(CoreConstants.nextIdStr())
					.appId(appId)
					.endpointId(endpointId)
					.subappId(args.getSubappId())
					.subappName(args.getSubappName())
					.subappIcon(args.getSubappIcon())
					.scope(scope.getScopeValue())
					.enabled(args.isEnabled())
					.sort((int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)))
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build())
					.build();

				return mongoTemplate.insert(newSubappMongodb, MongodbConstants.Collection.SUBAPP);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("createSubapp", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建子应用失败");
			}
		});

		if (subappMongodb != null) {
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_SUBAPP, subappMongodb.getAppId()),
				objectMapper.writeValueAsString(CreatedSubappMessage
					.builder()
					.id(subappMongodb.getId())
					.appId(subappMongodb.getAppId())
					.endpointId(subappMongodb.getEndpointId())
					.subappId(subappMongodb.getSubappId())
					.subappName(subappMongodb.getSubappName())
					.subappIcon(subappMongodb.getSubappIcon())
					.scope(subappMongodb.getScope())
					.enabled(subappMongodb.getEnabled())
					.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
					.eventTime(subappMongodb.getMetadata().getCreateTime())
					.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}

	}

	/**
	 * 修改子应用信息
	 * 锁：只允许一个用户对同一条子应用记录修改
	 *
	 * @param appId         应用ID
	 * @param endpointId 终端ID
	 * @param args          args
	 */
	@NewSpan
	@Lock4j(name = "modify_subapp_info", keys = {"#appId", "#endpointId", "#args.id"})
	@BizLog(
		bizId = "subapp:modify_subapp_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifySubappInfo(String appId, String endpointId, @Validated ModifySubappInfoArgs args) {

		// scope校验
		AccessScope scope;
		if (args.getScope() != null) {
			scope = AccessScope.scopeValueOf(args.getScope()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 范围：%s 错误", args.getScope())));

			// 范围超出应用允许范围就中断
			appCommonService.checkAppScope(mongoTemplate, appId, scope.getScopeValue());
		} else {
			scope = null;
		}
		SubappMongodb modifiedSubappMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria.where(SubappMongodb.FIELD.ID).is(args.getId()));
				Update update = new Update();

				if (args.getSubappId() != null) {
					update.set(SubappMongodb.FIELD.SUBAPP_ID, args.getSubappId());
				}

				if (args.getSubappName() != null) {
					update.set(SubappMongodb.FIELD.SUBAPP_NAME, args.getSubappName());
				}

				if (scope != null) {
					update.set(SubappMongodb.FIELD.SCOPE, scope.getScopeValue());
				}

				if (args.getSubappIcon() != null) {
					update.set(SubappMongodb.FIELD.SUBAPP_ICON, args.getSubappIcon());
				}

				update.currentDate(SubappMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(SubappMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyEndpoint", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改子应用信息失败");
			}
		});

		if (modifiedSubappMongodb == null) {
			throw new ConflictBusinessException("修改子应用信息失败");
		}

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_SUBAPP_INFO, modifiedSubappMongodb.getAppId()),
			objectMapper.writeValueAsString(ModifiedSubappInfoMessage.builder()
				.id(args.getId())
				.appId(modifiedSubappMongodb.getAppId())
				.appName(modifiedSubappMongodb.getAppId())
				.endpointId(modifiedSubappMongodb.getEndpointId())
				.endpointName(modifiedSubappMongodb.getEndpointId())
				.oldSubappId(modifiedSubappMongodb.getSubappId())
				.oldSubappName(modifiedSubappMongodb.getSubappName())
				.newSubappId(Optional.ofNullable(args.getSubappId()).orElse(args.getSubappId()))
				.newSubappName(Optional.ofNullable(args.getSubappName()).orElse(args.getSubappName()))
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);

	}

	/**
	 * 子应用开启/关闭
	 * 锁：一条记录同时只能被一个用户更改
	 *
	 * @param appId         应用ID
	 * @param endpointId 终端ID
	 * @param args          参数
	 */
	@NewSpan
	@Lock4j(name = "modify_subapp_status", keys = {"#appId", "#endpointId", "#args.id"})
	@BizLog(
		bizId = "subapp:modify_subapp_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifySubappStatus(String appId, String endpointId, @Validated ModifySubappStatusArgs args) {
		SubappMongodb subappMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(SubappMongodb.FIELD.ID).is(args.getId())
				);
				Update update = Update.update(SubappMongodb.FIELD.ENABLED, args.getEnabled());
				update.currentDate(SubappMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(SubappMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				return Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, SubappMongodb.class, MongodbConstants.Collection.SUBAPP))
					.orElseThrow(() -> new ConflictBusinessException("修改子应用状态失败"));
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifySubappStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改子应用状态失败");
			}
		});
		if (subappMongodb != null) {
			ModifiedSubappStatusMessage message = ModifiedSubappStatusMessage.builder()
				.id(subappMongodb.getId())
				.appId(subappMongodb.getAppId())
				.endpointId(subappMongodb.getEndpointId())
				.subappId(subappMongodb.getSubappId())
				.enabled(subappMongodb.getEnabled())
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(LocalDateTime.now())
				.build();
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_SUBAPP_STATUS, message.getAppId()),
				objectMapper.writeValueAsString(message),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}

	/**
	 * 移动 子应用
	 * 锁：同一个终端下只能一个用户移动子应用
	 *
	 * @param appId         应用ID
	 * @param endpointId 终端ID
	 * @param args          参数
	 */
	@NewSpan
	@Lock4j(name = "move_subapp", keys = {"#appId", "#endpointId"})
	@BizLog(
		bizId = "subapp:move_subapp",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void moveSubapp(@Valid @NotNull String appId, @Valid @NotNull String endpointId, MoveSubappArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				// endpointId 不合法就中断
				endpointCommonService.checkEndpointId(mongoTemplate, appId, endpointId);
				Criteria criteria = Criteria
					.where(SubappMongodb.FIELD.APP_ID).is(appId)
					.and(SubappMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(SubappMongodb.FIELD.ID).is(args.getMoveId1());
				Query subapp1Query = Query.query(criteria);
				SubappMongodb subapp1 = mongoTemplate.findOne(subapp1Query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
				Criteria swapCriteria = Criteria
					.where(SubappMongodb.FIELD.APP_ID).is(appId)
					.and(SubappMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(SubappMongodb.FIELD.ID).is(args.getMoveId2());
				Query subapp2Query = Query.query(swapCriteria);
				SubappMongodb subapp2 = mongoTemplate.findOne(subapp2Query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
				if (subapp1 == null || subapp2 == null) {
					throw new ConflictBusinessException("移动子应用失败（子应用不存在）");
				}

				// 临时排序值
				Update area1Update1 = Update.update(SubappMongodb.FIELD.SORT, LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)));
				// 目标排序值
				Update area1Update2 = Update.update(SubappMongodb.FIELD.SORT, Optional.of(subapp2.getSort()).orElse((int) (LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)))));

				// 临时排序值
				Update area2Update1 = Update.update(SubappMongodb.FIELD.SORT, LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)) + 1);
				// 目标排序值
				Update area2Update2 = Update.update(SubappMongodb.FIELD.SORT, Optional.of(subapp1.getSort()).orElse((int) (LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)))));

				// 临时排序值
				UpdateResult subapp1updateResult1 = mongoTemplate.updateFirst(subapp1Query, area1Update1, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
				UpdateResult subapp2updateResult1 = mongoTemplate.updateFirst(subapp2Query, area2Update1, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
				// 目标排序值
				UpdateResult subapp1updateResult2 = mongoTemplate.updateFirst(subapp1Query, area1Update2, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
				UpdateResult subapp2updateResult2 = mongoTemplate.updateFirst(subapp2Query, area2Update2, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);

			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("moveSubapp", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("移动子应用失败");
			}
		});
	}

	/**
	 * 删除子应用
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "delete_subapp", keys = {"#appId", "#endpointId", "#args.id"})
	@BizLog(
		bizId = "subapp:delete_subapp",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void deleteSubapp(String appId, String endpointId, @Validated DeleteSubappArgs args) {
		SubappMongodb deletedSubappMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria.where(SubappMongodb.FIELD.ID).is(args.getId()));
				SubappMongodb subappMongodb = mongoTemplate.findOne(query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
				if (subappMongodb == null) {
					throw new ConflictBusinessException("删除子应用失败（子应用不存在）");
				}
				Update update = new Update();
				update.set(SubappMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(SubappMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
				log.debug("deleteSubapp updateResult: {}", updateResult);
				SubappMongodb deletedSubapp = mongoTemplate.findAndRemove(query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);

				if (deletedSubapp == null) {
					throw new ConflictBusinessException("删除子应用失败（子应用不存在）");
				}
				SubappMongodb insert = mongoTemplate.insert(deletedSubapp, MongodbConstants.DeletedCollection.SUBAPP);
				return deletedSubapp;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteEndpoint", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除子应用失败");
			}
		});

		if (deletedSubappMongodb != null) {
			if (deletedSubappMongodb.getSubappIcon() != null) {
				fileCommonService.deletePublicFile(deletedSubappMongodb.getAppId().concat("/").concat(FileKeyPrefixConstants.SUBAPP_ICON_PREFIX), Collections.singletonList(deletedSubappMongodb.getSubappId()));
			}
			DeletedSubappMessage deletedSubappMessage = DeletedSubappMessage.builder()
				.appId(deletedSubappMongodb.getAppId())
				.endpointId(deletedSubappMongodb.getEndpointId())
				.subappId(deletedSubappMongodb.getSubappId())
				.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
				.eventTime(LocalDateTime.now())
				.build();
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_SUBAPP, deletedSubappMessage.getAppId()),
				objectMapper.writeValueAsString(deletedSubappMessage),
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
	private Criteria buildCriteria(@Valid @NotNull String appId, String endpointId, GetSubappArgs args) {
		Criteria criteria = new Criteria();

		if (appId != null && !appId.isBlank()) {
			criteria.and(SubappMongodb.FIELD.APP_ID).is(appId);
		}

		if (endpointId != null && !endpointId.isBlank()) {
			criteria.and(SubappMongodb.FIELD.ENDPOINT_ID).is(endpointId);
		}

		if (args.getEnabled() != null) {
			criteria.and(SubappMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.and(SubappMongodb.FIELD.SUBAPP_NAME).regex(args.getKeyword());
		}

		return criteria;
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return metadata subapp list
	 */
	List<MetadataSubapp> getSubappList(List<SubappMongodb> ms) {
		List<String> appIds = ms.stream().map(SubappMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		List<String> endpointIds = ms.stream().map(SubappMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = Optional.of(endpointIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(endpointCommonService::getEndpointMapByEndpointIds)
			.orElse(Collections.emptyMap());

		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(SubappMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds))
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> SubappConverter.convertMetadataSubapp(x, appMap, endpointMap, metadataUserMap)).collect(Collectors.toList());
	}
}
