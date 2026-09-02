package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.subapp_version;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappVersionMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.MetadataSubappVersion;
import io.github.lijiajia3515.cairo.auth.modules.subapp_version.SubappVersionVersionConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.CreateSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.DeleteSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.GetSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.ModifySubappVersionInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.ModifySubappVersionStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.SyncSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp_version.DeletedSubappVersionMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp_version.SyncedSubappVersionMessage;
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
 * [cairo-web-manage/api] subapp_version service
 */
@Slf4j
@Validated
@Component
public class SubappVersionCairoWebManageApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final SubappCommonService subappCommonService;
	private final AppUserCommonService appUserCommonService;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;

	public SubappVersionCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
												  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												  TransactionTemplate transactionTemplate,
												  CairoSecurityProperties cairoSecurityProperties,
												  SubappCommonService subappCommonService,
												  AppUserCommonService appUserCommonService,
												  RabbitTemplate rabbitTemplate,
												  CairoRabbitmqTool cairoRabbitmqTool,
												  ObjectMapper objectMapper) {
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.subappCommonService = subappCommonService;
		this.appUserCommonService = appUserCommonService;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
	}


	/**
	 * 获取子应用版本集合
	 *
	 * @param args 参数
	 * @return 端点集合
	 */
	@NewSpan
	@BizLog(
		bizId = "subapp_version:get_subapp_version_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataSubappVersion> getSubappVersionList(@Validated GetSubappVersionArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(SubappVersionMongodb.FIELD.SUBAPP_ID),
					Sort.Order.desc(SubappVersionMongodb.FIELD.SUBAPP_VERSION),
					Sort.Order.desc(SubappVersionMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<SubappVersionMongodb> tms = readMongoTemplate.find(query, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
		return getSubappVersionList(tms);
	}

	/**
	 * 子应用版本分页查询
	 *
	 * @return 子应用版本分页查询
	 */
	@NewSpan
	@BizLog(
		bizId = "subapp_version:get_subapp_version_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataSubappVersion> getSubappVersionPageList(@Validated GetSubappVersionArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(SubappVersionMongodb.FIELD.SUBAPP_ID),
					Sort.Order.desc(SubappVersionMongodb.FIELD.SUBAPP_VERSION),
					Sort.Order.desc(SubappVersionMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		long total = readMongoTemplate.count(query, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(SubappVersionMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<MetadataSubappVersion> ds = getSubappVersionList(readMongoTemplate.find(query, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION));

		return new Page<>(args, ds, total);
	}


	/**
	 * 创建子应用版本
	 *
	 * @param args 参数
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "subapp_version:create_subapp_version",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createSubappVersion(@Validated CreateSubappVersionArgs args) {

		Criteria subappCriteria = Criteria
			.where(SubappMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());

		Query producQuery = Query.query(subappCriteria);
		boolean exists = mongoTemplate.exists(producQuery, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);

		if (!exists) {
			throw new ConflictBusinessException("子应用不存在");
		}

		Criteria subappVersionCriteria = Criteria
			.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
			.and(SubappVersionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion());

		Query subappVersionQuery = Query.query(subappVersionCriteria);
		boolean subappVersionExists = mongoTemplate.exists(subappVersionQuery, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);

		if (subappVersionExists) {
			throw new ConflictBusinessException("子应用版本已存在");
		}

		transactionTemplate.executeWithoutResult(status -> {
			try {
				SubappVersionMongodb build = SubappVersionMongodb.builder()
					.subappId(args.getSubappId())
					.subappVersion(args.getSubappVersion())
					.subappRemark(args.getSubappRemark())
					.enabled(true)
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build())
					.build();
				mongoTemplate.insert(build, MongodbConstants.Collection.SUBAPP_VERSION);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("createSubappVersion", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建子应用版本失败");
			}
		});

	}

	/**
	 * 修改子应用版本信息
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_subapp_version_info", keys = {"#args.subappId", "#args.subappVersion"})
	@BizLog(
		bizId = "subapp_version:modify_subapp_version_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifySubappVersionInfo(@Validated ModifySubappVersionInfoArgs args) {
		Criteria subappCriteria = Criteria
			.where(SubappMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());

		Query producQuery = Query.query(subappCriteria);
		boolean exists = mongoTemplate.exists(producQuery, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);

		if (!exists) {
			throw new ConflictBusinessException("子应用不存在");
		}

		Criteria subappVersionCriteria = Criteria
			.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
			.and(SubappVersionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion());

		Query subappVersionQuery = Query.query(subappVersionCriteria);
		boolean subappVersionExists = mongoTemplate.exists(subappVersionQuery, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);

		if (!subappVersionExists) {
			throw new ConflictBusinessException("子应用版本不存在");
		}

		SubappVersionMongodb modifiedSubappVersionMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
					.and(SubappVersionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion()));
				Update update = new Update();

				if (args.getSubappRemark() != null) {
					update.set(SubappVersionMongodb.FIELD.SUBAPP_REMARK, args.getSubappRemark());
				}

				update.currentDate(SubappVersionMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(SubappVersionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifySubappVersionInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改子应用版本信息失败");
			}
		});

		if (modifiedSubappVersionMongodb == null) {
			throw new ConflictBusinessException("修改子应用版本信息失败");
		}
	}

	/**
	 * 子应用版本开启/关闭
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_subapp_version_status", keys = {"#args.subappId", "#args.subappVersion"})
	@BizLog(
		bizId = "subapp_version:modify_subapp_version_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifySubappVersionStatus(@Validated ModifySubappVersionStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
						.and(SubappVersionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion())
				);
				Update update = Update.update(SubappVersionMongodb.FIELD.ENABLED, args.getEnabled());
				update.currentDate(SubappVersionMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(SubappVersionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION))
					.orElseThrow(() -> new ConflictBusinessException("修改子应用版本状态失败"));
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifySubappVersionStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改子应用版本状态失败");
			}
		});
	}


	/**
	 * 删除子应用版本
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "delete_subapp_version", keys = {"#args.subappId", "#args.subappVersion"})
	@BizLog(
		bizId = "subapp_version:delete_subapp_version",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void deleteSubappVersion(@Validated DeleteSubappVersionArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				// 子应用
				Criteria subappCriteria = Criteria
					.where(SubappMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
				Query subappSubappQuery = Query.query(subappCriteria);

				SubappMongodb subappMongodb = mongoTemplate.findOne(subappSubappQuery, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
				if (subappMongodb == null) {
					throw new ConflictBusinessException("子应用不存在");
				}
				Query query = Query.query(Criteria.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
					.and(SubappVersionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion())
				);
				SubappVersionMongodb subappVersionMongodb = mongoTemplate.findOne(query, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
				if (subappVersionMongodb == null) {
					throw new ConflictBusinessException("删除子应用版本失败（子应用版本不存在）");
				}
				Update update = new Update();
				update.set(SubappVersionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(SubappVersionMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
				log.debug("deleteSubapp updateResult: {}", updateResult);
				SubappVersionMongodb deletedSubappVersion = mongoTemplate.findAndRemove(query, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);

				if (deletedSubappVersion == null) {
					throw new ConflictBusinessException("删除子应用版本失败");
				}
				mongoTemplate.insert(deletedSubappVersion, MongodbConstants.DeletedCollection.SUBAPP_VERSION);

				// 发送删除子应用版本消息
				rabbitTemplate.convertAndSend(
					cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
					cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_SUBAPP_VERSION, subappMongodb.getAppId()),
					objectMapper.writeValueAsString(DeletedSubappVersionMessage.builder()
						.appId(subappMongodb.getAppId())
						.endpointId(subappMongodb.getEndpointId())
						.subappId(subappVersionMongodb.getSubappId())
						.subappVersion(subappVersionMongodb.getSubappVersion())
						.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
						.eventTime(LocalDateTime.now())
						.build()
					),
					new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
				);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteSubappVersion", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除子应用版本失败");
			}
		});

	}

	/**
	 * 同步子应用版本
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "sync_subapp_version", keys = {"#args.changeSubappId", "#args.changeSubappVersion"})
	@BizLog(
		bizId = "subapp_version:sync_subapp_version",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void syncSubappVersion(@Validated SyncSubappVersionArgs args) {
		Criteria criteria = Criteria
			.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSourceSubappId())
			.and(SubappVersionMongodb.FIELD.SUBAPP_VERSION).is(args.getSourceSubappVersion());
		Query query = Query.query(criteria);

		SubappVersionMongodb sourceSubappVersionMongodb = mongoTemplate.findOne(query, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
		if (sourceSubappVersionMongodb == null) {
			throw new ConflictBusinessException("数据来源子应用版本不存在");
		}

		// 子应用
		Criteria afterSubappCriteria = Criteria
			.where(SubappMongodb.FIELD.SUBAPP_ID).is(args.getChangeSubappId());
		Query afterSubappQuery = Query.query(afterSubappCriteria);

		SubappMongodb changeSubappMongodb = mongoTemplate.findOne(afterSubappQuery, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
		if (changeSubappMongodb == null) {
			throw new ConflictBusinessException("数据变更子应用不存在");
		}

		Criteria changeCriteria = Criteria
			.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getChangeSubappId())
			.and(SubappVersionMongodb.FIELD.SUBAPP_VERSION).is(args.getChangeSubappVersion());
		Query afterQuery = Query.query(changeCriteria);

		SubappVersionMongodb changeSubappVersionMongodb = mongoTemplate.findOne(afterQuery, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
		if (changeSubappVersionMongodb == null) {
			throw new ConflictBusinessException("数据变更子应用版本不存在");
		}
		Update update = Update.update(SubappVersionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
		update.currentDate(SubappVersionMongodb.FIELD.METADATA.UPDATE_TIME);

		final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
		SubappVersionMongodb modifiedSubappVersionMongodb = mongoTemplate.findAndModify(afterQuery, update, options, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
		log.info("syncSubappVersion modify{}", modifiedSubappVersionMongodb);

		// 发送子应用版本同步消息
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.SYNCED_SUBAPP_VERSION, changeSubappMongodb.getAppId()),
			objectMapper.writeValueAsString(SyncedSubappVersionMessage.builder()
				.sourceSubappId(sourceSubappVersionMongodb.getSubappId())
				.sourceSubappVersion(sourceSubappVersionMongodb.getSubappVersion())
				.changeSubappId(changeSubappVersionMongodb.getSubappId())
				.changeSubappVersion(changeSubappVersionMongodb.getSubappVersion())
				.changeAppId(changeSubappMongodb.getAppId())
				.changeEndpointId(changeSubappMongodb.getEndpointId())
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
	private Criteria buildCriteria(GetSubappVersionArgs args) {
		Criteria criteria = new Criteria();

		if (args.getSubappId() != null && !args.getSubappId().isBlank()) {
			criteria.and(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
		}

		if (args.getEnabled() != null) {
			criteria.and(SubappVersionMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(SubappVersionMongodb.FIELD.SUBAPP_VERSION).regex(args.getKeyword()),
				Criteria.where(SubappVersionMongodb.FIELD.SUBAPP_REMARK).regex(args.getKeyword()));
		}
		return criteria;
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return metadata subapp_version list
	 */
	List<MetadataSubappVersion> getSubappVersionList(List<SubappVersionMongodb> ms) {
		List<String> subappIds = ms.stream().map(SubappVersionMongodb::getSubappId).distinct().collect(Collectors.toList());
		Map<String, Subapp> subappMap = Optional.of(subappIds)
			.filter(pIds -> !pIds.isEmpty())
			.map(subappCommonService::getSubappMapBySubappIds)
			.orElse(Collections.emptyMap());

		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(SubappVersionMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds))
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> SubappVersionVersionConverter.convertMetadataSubapp(x, subappMap, metadataUserMap)).collect(Collectors.toList());
	}
}
