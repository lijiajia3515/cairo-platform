package io.github.lijiajia3515.cairo.auth.api.subapp.tenant_app_role_template;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappVersionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleTemplatePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.MetadataTenantAppRoleTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplate;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role_template.TenantAppRoleTemplateConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplateExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplateField;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplatePermissionNode;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplateSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.CreateTenantAppRoleTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.DeleteTenantAppRoleTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.DeleteTenantAppRoleTemplatePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.GetTenantAppRoleTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.ModifyTenantAppRoleTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.ModifyTenantAppRoleTemplatePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.ModifyTenantAppRoleTemplateStatusArgs;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import io.github.lijiajia3515.cairo.mongodb.serial.SerialService;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;


/**
 * [tenant_app_role_template/api] service
 */
@Slf4j
@Validated
@Component
public class TenantAppRoleTemplateSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppUserCommonService appUserCommonService;
	private static final String SERIAL_NAMESPACE = "default";
	private static final String SERIAL_KEY = "tenant_app_role_template";

	private final SerialService serialService;


	public TenantAppRoleTemplateSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
													 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													 TransactionTemplate transactionTemplate,
													 AppUserCommonService appUserCommonService, SerialService serialService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appUserCommonService = appUserCommonService;
		this.serialService = serialService;
	}


	/**
	 * get tenant_app_role_template list
	 *
	 * @param appId appId
	 * @param args  args
	 * @return tenant_app_role_template list
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_role_template:get_tenant_app_role_template_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataTenantAppRoleTemplate> getTenantAppRoleTemplateList(@Valid @NotNull String appId, @Validated GetTenantAppRoleTemplateArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_TIME)));
		final List<TenantAppRoleTemplateMongodb> rms = readMongoTemplate.find(query, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);
		return getTenantAppRoleTemplateList(appId,rms, args.getExtension());
	}


	/**
	 * get tenant_app_role_template page list
	 *
	 * @param appId 应用id
	 * @param args  args
	 * @return tenant_app_role_template page
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_role_template:get_tenant_app_role_template_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	Page<MetadataTenantAppRoleTemplate> getTenantAppRoleTemplatePageList(@Valid @NotNull String appId, @Validated GetTenantAppRoleTemplateArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);

		query.with(args.pageable());
		query.with(Sort.by(
			Sort.Order.desc(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
		));
		List<TenantAppRoleTemplateMongodb> rms = readMongoTemplate.find(query, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);
		final List<MetadataTenantAppRoleTemplate> rs = getTenantAppRoleTemplateList(appId,rms, args.getExtension());
		return new Page<>(args, rs, total);
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_role_template:get_tenant_app_role_template_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenant_app_role_template_by_id", value = "#roleTemplateId"),
		}
	)
	Optional<TenantAppRoleTemplate> getTenantAppRoleTemplateInfo(String appId, @Valid @NotNull String roleTemplateId) {
		Query query = Query.query(Criteria.where(TenantAppRoleTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRoleTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(roleTemplateId));
		TenantAppRoleTemplateMongodb role = readMongoTemplate.findOne(query, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);
		return Optional.ofNullable(role).map(TenantAppRoleTemplateConverter::convert);
	}

	@NewSpan
	List<TenantAppRoleTemplatePermissionNode> getTenantAppRoleTemplatePermission(@Valid @NotNull String appId, @Valid @NotNull String roleId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion) {
		Query menuRootQuery = Query.query(Criteria.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.MENU_ID).is(ROOT_ID)
		);
		MenuMongodb parentResource = mongoTemplate.findOne(menuRootQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

		if (parentResource == null) {
			return null;
		}

		Criteria menuCriteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.LEFT_NO).gte(parentResource.getLeftNo())
			.and(MenuMongodb.FIELD.RIGHT_NO).lte(parentResource.getRightNo())
			.and(MenuMongodb.FIELD.HIDDEN_MENU).is(false);
		Query menuQuery = Query.query(menuCriteria);
		menuQuery.with(Sort.by(
			Sort.Order.asc(MenuMongodb.FIELD.DEPTH),
			Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)
		));

		List<MenuMongodb> menus = mongoTemplate.find(menuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

		Criteria appRolePermissionCriteria = Criteria
			.where(TenantAppRoleTemplatePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(roleId)
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);
		Query appRolePermissionQuery = Query.query(appRolePermissionCriteria);
		Set<String> appRolePermissionIds = Optional.ofNullable(mongoTemplate.findOne(appRolePermissionQuery, TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION))
			.map(TenantAppRoleTemplatePermissionMongodb::getPermissionIds)
			.<Set<String>>map(HashSet::new)
			.orElse(Collections.emptySet());


		Set<String> menuIds = menus.stream().map(MenuMongodb::getMenuId).collect(Collectors.toSet());
		Map<String, List<PermissionMongodb>> permissionMap = Optional.of(menuIds).filter(x -> !x.isEmpty()).map(mIds -> {
			Query permissionQuery = Query.query(Criteria
				.where(PermissionMongodb.FIELD.APP_ID).is(appId)
				.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
				.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
				.and(PermissionMongodb.FIELD.MENU_ID).in(mIds)
				.and(PermissionMongodb.FIELD.HIDDEN_PERMISSION).is(false)
			);
			permissionQuery.with(Sort.by(
				Sort.Order.asc(PermissionMongodb.FIELD.SORT)
			));
			return readMongoTemplate.find(permissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION).stream()
				.collect(Collectors.groupingBy(PermissionMongodb::getMenuId));

		}).orElse(Collections.emptyMap());

		List<TenantAppRoleTemplatePermissionNode> nodes = menus.stream()
			.map(x -> TenantAppRoleTemplatePermissionNode.builder()
				.menuId(x.getMenuId())
				.parentId(x.getParentId())
				.menuName(x.getMenuName())
				.path(x.getPath())
				.component(x.getComponent())
				.icon(x.getIcon())
				.hiddenMenu(x.isHiddenMenu())
				.depth(x.getDepth())
				.leftNo(x.getLeftNo())
				.rightNo(x.getRightNo())
				.permissions(
					permissionMap.getOrDefault(x.getMenuId(), Collections.emptyList())
						.stream().map(e -> TenantAppRoleTemplatePermissionNode.Permission.builder()
							.permissionId(e.getPermissionId())
							.permissionName(e.getPermissionName())
							.type(e.getType())
							.defaultPermission(e.getDefaultPermission())
							.hiddenPermission(e.getHiddenPermission())
							.isSelected(appRolePermissionIds.contains(e.getPermissionId()))
							.icon(e.getIcon())
							.sort(e.getSort())
							.build()
						).collect(Collectors.toList())
				)
				.build()
			)
			.collect(Collectors.toList());
		return Tree2Converter.build(nodes, ROOT_ID);
	}

	/**
	 * 修改企业角色模板
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_role_template:create_tenant_app_role_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createTenantAppRoleTemplate(String appId, @Validated CreateTenantAppRoleTemplateArgs args) {
		TenantAppRoleTemplateMongodb insertedTenantAppRoleTemplate = transactionTemplate.execute(status -> {
			try {
				TenantAppRoleTemplateMongodb tenantAppRoleTemplateMongodb = TenantAppRoleTemplateMongodb.builder()
					.appId(appId)
					.tenantAppRoleTemplateId(serialService.nextStr(SERIAL_NAMESPACE, SERIAL_KEY,1,1001))
					.tenantAppRoleTemplateName(args.getTenantAppRoleTemplateName())
					.remark(args.getRemark())
					.enabled(true)
					.sort(System.currentTimeMillis())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build()
					)
					.build();
				return mongoTemplate.insert(tenantAppRoleTemplateMongodb, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);
			} catch (Exception e) {
				log.info("createTenantAppRoleTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建企业角色模板失败");
			}
		});
		if (insertedTenantAppRoleTemplate == null) {
			throw new ConflictBusinessException("创建企业角色模板失败");
		}
	}

	/**
	 * 修改企业角色模板信息
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_role_template_info", keys = {"#args.tenantAppRoleTemplateId"})
	@BizLog(
		bizId = "tenant_app_role_template:modify_tenant_app_role_template_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppRoleTemplateInfo(String appId, @Validated ModifyTenantAppRoleTemplateInfoArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppRoleTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(args.getTenantAppRoleTemplateId())
				);
				Update update = new Update();
				Optional.ofNullable(args.getTenantAppRoleTemplateName()).ifPresent(name -> update.set(TenantAppRoleTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_NAME, name));
				Optional.ofNullable(args.getRemark()).ifPresent(remark -> update.set(TenantAppRoleTemplateMongodb.FIELD.REMARK, remark));

				update.set(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult result = mongoTemplate.updateFirst(query, update, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);

				if (result.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业角色模板失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyTenantAppRoleTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业角色模板失败");
			}
		});
	}

	/**
	 * 修改企业角色模板权限
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_role_template_permission", keys = {"#args.endpointId", "#args.subappId", "#args.subappVersion", "#args.tenantAppRoleTemplateId"})
	@BizLog(
		bizId = "tenant_app_role_template:modify_tenant_app_role_template_permission",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppRoleTemplatePermission(String appId, @Validated ModifyTenantAppRoleTemplatePermissionArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query tenantAppRoleTemplatePermissionQuery = Query.query(Criteria
					.where(TenantAppRoleTemplatePermissionMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleTemplatePermissionMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(args.getTenantAppRoleTemplateId())
					.and(TenantAppRoleTemplatePermissionMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
					.and(TenantAppRoleTemplatePermissionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
					.and(TenantAppRoleTemplatePermissionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion())
				);
				boolean exists = mongoTemplate.exists(tenantAppRoleTemplatePermissionQuery, TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
				if (exists) {
					Update update = new Update();
					update.set(TenantAppRoleTemplatePermissionMongodb.FIELD.PERMISSION_IDS, args.getPermissionIds());
					update.set(TenantAppRoleTemplatePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
					update.currentDate(TenantAppRoleTemplatePermissionMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult tenantAppRoleTemplatePermissionUpdateResult = mongoTemplate.updateFirst(tenantAppRoleTemplatePermissionQuery, update, TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
					if (tenantAppRoleTemplatePermissionUpdateResult.getModifiedCount() < 1) {
						throw new ConflictBusinessException("修改企业角色模板权限失败");
					}
				} else {
					TenantAppRoleTemplatePermissionMongodb insertTenantAppRoleTemplatePermission = TenantAppRoleTemplatePermissionMongodb.builder()
						.appId(appId)
						.endpointId(args.getEndpointId())
						.subappId(args.getSubappId())
						.subappVersion(args.getSubappVersion())
						.tenantAppRoleTemplateId(args.getTenantAppRoleTemplateId())
						.permissionIds(args.getPermissionIds())
						.metadata(AppUserMetadataMongodb.builder()
							.createUserId(CairoSecurityContextHolder.getSubappUserId())
							.updateUserId(CairoSecurityContextHolder.getSubappUserId())
							.build())
						.build();
					mongoTemplate.insert(insertTenantAppRoleTemplatePermission, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
				}
				Criteria criteria = Criteria
					.where(TenantAppRoleTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(args.getTenantAppRoleTemplateId());
				Query query = Query.query(criteria);
				Update tenantAppRoleTemplateUpdate = Update.update(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				tenantAppRoleTemplateUpdate.currentDate(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult result = mongoTemplate.updateFirst(query, tenantAppRoleTemplateUpdate, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);
				if (result.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业角色模板权限失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyTenantAppRoleTemplatePermission", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业角色模板权限失败");
			}
		});
	}

	/**
	 * 修改企业角色模板状态
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_role_template_status", keys = {"#args.tenantAppRoleTemplateId"})
	@BizLog(
		bizId = "tenant_app_role_template:modify_tenant_app_role_template_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppRoleTemplateStatus(String appId, @Validated ModifyTenantAppRoleTemplateStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppRoleTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(args.getTenantAppRoleTemplateId())
				);
				Update update = new Update();
				update.set(TenantAppRoleTemplateMongodb.FIELD.ENABLED, args.getEnabled());

				update.set(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult result = mongoTemplate.updateFirst(query, update, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);

				if (result.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改企业角色模板状态失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyTenantAppRoleTemplateStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业角色模板状态失败");
			}
		});
	}

	/**
	 * 删除
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_app_role_templates", keys = {"#args.tenantAppRoleTemplateId"})
	@BizLog(
		bizId = "tenant_app_role_template:delete_tenant_app_role_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteTenantAppRoleTemplate(String appId, @Validated DeleteTenantAppRoleTemplateArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria appRoleCriteria = Criteria
					.where(TenantAppRoleTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(args.getTenantAppRoleTemplateId());
				Query appRoleQuery = Query.query(appRoleCriteria);

				Update update = new Update();
				update.set(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_TIME);

				mongoTemplate.updateMulti(appRoleQuery, update, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);
				List<TenantAppRoleTemplateMongodb> deletedTenantAppRoleTemplateMongodbList = mongoTemplate.findAllAndRemove(appRoleQuery, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);
				if (!deletedTenantAppRoleTemplateMongodbList.isEmpty()) {
					mongoTemplate.insert(deletedTenantAppRoleTemplateMongodbList, MongodbConstants.DeletedCollection.TENANT_APP_ROLE_TEMPLATE);
				}

				Criteria appRolePermissionCriteria = Criteria
					.where(TenantAppRoleTemplatePermissionMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(args.getTenantAppRoleTemplateId());
				Query appRolePermissionQuery = Query.query(appRolePermissionCriteria);
				Update appRolePermissionUpdate = new Update();
				appRolePermissionUpdate.set(TenantAppRoleTemplatePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				appRolePermissionUpdate.currentDate(TenantAppRoleTemplatePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

				mongoTemplate.updateMulti(appRolePermissionQuery, appRolePermissionUpdate, TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
				List<TenantAppRoleTemplatePermissionMongodb> deleteTenantAppRoleTemplatePermissionMongodbList = mongoTemplate.findAllAndRemove(appRolePermissionQuery, TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
				if (!deleteTenantAppRoleTemplatePermissionMongodbList.isEmpty()) {
					mongoTemplate.insert(deleteTenantAppRoleTemplatePermissionMongodbList, MongodbConstants.DeletedCollection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("deleteTenantAppRoleTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除企业角色模板失败");
			}
		});
	}

	/**
	 * 查看企业角色模板子应用版本
	 *
	 * @param endpointId endpointId
	 * @param roleId        roleId
	 * @param subappId     subappId
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_role_template_subapp_version:get_tenant_app_role_template_subapp_version",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "roleId", value = "#roleId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
		}
	)
	public List<TenantAppRoleTemplateSubappVersion> getTenantAppRoleTemplateSubappVersion(String appId, String endpointId, String roleId, String subappId) {
		//查询企业角色模板权限
		Criteria appRolePermissionCriteria = Criteria
			.where(TenantAppRoleTemplatePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(roleId)
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.SUBAPP_ID).is(subappId);
		Query appRolePermissionQuery = Query.query(appRolePermissionCriteria);
		List<TenantAppRoleTemplatePermissionMongodb> tenantAppRoleTemplatePermissions = mongoTemplate.find(appRolePermissionQuery, TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);


		//查询子应用版本
		Criteria subappVersionCriteria = Criteria
			.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(subappId);
		Query subappVersionQuery = Query.query(subappVersionCriteria);
		List<SubappVersionMongodb> subappVersionMongodbs = mongoTemplate.find(subappVersionQuery, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);


		return subappVersionMongodbs.stream().map(version -> {
			TenantAppRoleTemplatePermissionMongodb rolePermissionMongodb = tenantAppRoleTemplatePermissions.stream().filter(p -> p.getSubappVersion().equals(version.getSubappVersion())).findFirst().orElse(null);
			if (rolePermissionMongodb != null) {
				return TenantAppRoleTemplateSubappVersion.builder()
					.subappId(version.getSubappId())
					.subappVersion(version.getSubappVersion())
					.subappRemark(version.getSubappRemark())
					.enabled(true)
					.build();
			} else {
				return TenantAppRoleTemplateSubappVersion.builder()
					.subappId(version.getSubappId())
					.subappVersion(version.getSubappVersion())
					.subappRemark(version.getSubappRemark())
					.enabled(false)
					.build();
			}
		}).collect(Collectors.toList());

	}

	/**
	 * 删除企业角色模板权限
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_app_role_template_permission", keys = {"#args.tenantAppRoleTemplateId", "#args.endpointId", "#args.subappId", "#args.subappVersion"})
	@BizLog(
		bizId = "tenant_app_role_template:delete_tenant_app_role_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteTenantAppRoleTemplatePermission(String appId, DeleteTenantAppRoleTemplatePermissionArgs args) {
		//查询是否开通企业角色模板权限
		Criteria appRolePermissionCriteria = Criteria
			.where(TenantAppRoleTemplatePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).is(args.getTenantAppRoleTemplateId())
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
			.and(TenantAppRoleTemplatePermissionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion());
		Query appRolePermissionQuery = Query.query(appRolePermissionCriteria);
		TenantAppRoleTemplatePermissionMongodb appRolePermissionMongodb = mongoTemplate.findOne(appRolePermissionQuery, TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
		if (appRolePermissionMongodb == null) {
			throw new ConflictBusinessException("企业角色模板权限未开通");
		}
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria deleteTenantAppRoleTemplatePermissionCriteria = Criteria
					.where(TenantAppRoleTemplatePermissionMongodb.FIELD._ID).is(appRolePermissionMongodb.get_id());
				Query deleteTenantAppRoleTemplatePermissionQuery = Query.query(deleteTenantAppRoleTemplatePermissionCriteria);
				Update deleteTenantAppRoleTemplatePermissionUpdate = new Update();
				deleteTenantAppRoleTemplatePermissionUpdate.set(TenantAppRoleTemplatePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				deleteTenantAppRoleTemplatePermissionUpdate.currentDate(TenantAppRoleTemplatePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

				mongoTemplate.updateMulti(deleteTenantAppRoleTemplatePermissionQuery, deleteTenantAppRoleTemplatePermissionUpdate, TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
				List<TenantAppRoleTemplatePermissionMongodb> deleteTenantAppRoleTemplatePermissionMongodbList = mongoTemplate.findAllAndRemove(deleteTenantAppRoleTemplatePermissionQuery, TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
				if (!deleteTenantAppRoleTemplatePermissionMongodbList.isEmpty()) {
					mongoTemplate.insert(deleteTenantAppRoleTemplatePermissionMongodbList, MongodbConstants.DeletedCollection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("deleteTenantAppRoleTemplatePermission", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除企业角色模板权限失败");
			}
		});

	}

	@NewSpan
	List<MetadataTenantAppRoleTemplate> getTenantAppRoleTemplateList(String appId,List<TenantAppRoleTemplateMongodb> ms, Map<String, String> extensionMap) {
		TenantAppRoleTemplateExtension extension = Optional.ofNullable(extensionMap.get(CairoAuthExtensionConstants.TENANT_APP_ROLE_TEMPLATE)).map(TenantAppRoleTemplateExtension::valueOf).orElse(TenantAppRoleTemplateExtension.ALL);

		Map<String, Integer> userCountMap = new HashMap<>();
		if (extension.fields().contains(TenantAppRoleTemplateField.USER_NUM)) {
			Set<String> roleIds = ms.stream().map(TenantAppRoleTemplateMongodb::getTenantAppRoleTemplateId).collect(Collectors.toSet());
			if (!roleIds.isEmpty()) {
				final Field fieldKey = Fields.field(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_IDS);
				userCountMap.putAll(readMongoTemplate.aggregate(Aggregation.newAggregation(
						Aggregation.match(Criteria
							.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId)
							.and(fieldKey.getTarget()).elemMatch(new Criteria().in(roleIds))
						),
						Aggregation.project(Fields.from(fieldKey)),
						Aggregation.unwind(fieldKey.getName()),
						Aggregation.group(fieldKey.getName()).count().as("Num"),
						Aggregation.sort(Sort.by(Sort.Order.desc(fieldKey.getName())))
					), MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE, BasicDBObject.class).getMappedResults().stream()
					.collect(Collectors.toMap(z -> z.getString("_id"), z -> z.getInt("Num"))));
			}
		}



		Map<String, AppUser> metadataUserMap = new HashMap<>();
		if (extension.fields().contains(TenantAppRoleTemplateField.METADATA)) {
			Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(TenantAppRoleTemplateMongodb::getMetadata).collect(Collectors.toList()));
			Map<String, AppUser> userMap = appUserCommonService.getAppUserMapByAppUserIds(appId, metadataUserIds);
			metadataUserMap.putAll(userMap);
		}

		return ms.stream()
			.map(x -> TenantAppRoleTemplateConverter.convert(x, metadataUserMap,userCountMap, extension))
			.collect(Collectors.toList());
	}


	Criteria buildCriteria(String appId, GetTenantAppRoleTemplateArgs args) {
		Criteria criteria = Criteria.where(TenantAppRoleTemplateMongodb.FIELD.APP_ID).is(appId);
		Optional.ofNullable(args.getKeyword()).filter(kw -> !kw.isEmpty()).ifPresent(kw -> criteria.and(TenantAppRoleTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_NAME).regex(kw));
		Optional.ofNullable(args.getTenantAppRoleTemplateIds()).filter(r -> !r.isEmpty()).ifPresent(r -> criteria.and(TenantAppRoleTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).in(r));
		Optional.ofNullable(args.getEnabled()).ifPresent(e -> criteria.and(TenantAppRoleTemplateMongodb.FIELD.ENABLED).is(e));
		return criteria;
	}

}
