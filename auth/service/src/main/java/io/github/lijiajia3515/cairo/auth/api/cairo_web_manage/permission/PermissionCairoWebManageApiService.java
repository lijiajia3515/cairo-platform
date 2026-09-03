package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.permission;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.modules.permission.PermissionConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.MetadataPermission;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.CreatePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.DeletePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.GetPermissionListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.GetPermissionPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.ModifyPermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.MovePermissionArgs;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.file.FileCommonService;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.PathMenu;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.modules.subapp_version.SubappVersionCommonService;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;

/**
 * [cairo_endpoint_user/api] action permission service
 */
@Slf4j
@Service
@Validated
public class PermissionCairoWebManageApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final MenuCommonService menuCommonService;
	private final AppUserCommonService appUserCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final FileCommonService fileCommonService;
	private final SubappCommonService subappCommonService;
	private final SubappVersionCommonService subappVersionCommonService;

	public PermissionCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
													@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													TransactionTemplate transactionTemplate,
													AppCommonService appCommonService,
													EndpointCommonService endpointCommonService,
													MenuCommonService menuCommonService,
													AppUserCommonService appUserCommonService,
													CairoSecurityProperties cairoSecurityProperties,
													FileCommonService fileCommonService,
													SubappCommonService subappCommonService,
													SubappVersionCommonService subappVersionCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.menuCommonService = menuCommonService;
		this.appUserCommonService = appUserCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.fileCommonService = fileCommonService;
		this.subappCommonService = subappCommonService;
		this.subappVersionCommonService = subappVersionCommonService;
	}

	/**
	 * 获取功能权限 集合模式
	 *
	 * @param appId 应用id
	 * @param args  参数
	 * @return 功能权限 list
	 */
	@NewSpan
	@BizLog(
		bizId = "permission:get_permission_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MetadataPermission> getPermissionList(@Valid @NotNull String appId, @Valid String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetPermissionListArgs args) {
		checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
		Criteria criteria = Criteria
			.where(PermissionMongodb.FIELD.APP_ID).is(appId)
			.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);
		Optional.ofNullable(args.getMenuIds()).filter(x -> !x.isEmpty()).ifPresent(menuId -> criteria.and(PermissionMongodb.FIELD.MENU_ID).in(menuId));
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.by(PermissionMongodb.FIELD.SORT)
		));
		List<PermissionMongodb> list = readMongoTemplate.find(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		return getMetadataPermissionList(appId, endpointId, subappId, subappVersion, list);
	}

	/**
	 * 获取元素菜单 分页模式
	 *
	 * @param appId 应用id
	 * @param args  参数
	 * @return 元素 分页对象
	 */
	@NewSpan
	@BizLog(
		bizId = "permission:get_permission_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataPermission> getPermissionPageList(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetPermissionPageListArgs args) {
		checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
		Criteria criteria = Criteria
			.where(PermissionMongodb.FIELD.APP_ID).is(appId)
			.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

		Optional.ofNullable(args.getMenuIds()).filter(x -> !x.isEmpty()).ifPresent(menuId -> criteria.and(PermissionMongodb.FIELD.MENU_ID).in(menuId));
		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.by(PermissionMongodb.FIELD.SORT)));

		long total = readMongoTemplate.count(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		query.with(args.pageable());
		List<PermissionMongodb> ms = readMongoTemplate.find(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		List<MetadataPermission> metadataPermissionList = getMetadataPermissionList(appId, endpointId, subappId, subappVersion, ms);
		return new Page<>(args, metadataPermissionList, total);
	}


	/**
	 * 创建功能权限
	 * 锁：一个子应用版本下只允许一个并发添加
	 *
	 * @param appId          应用ID
	 * @param endpointId  终端ID
	 * @param subappId      子应用ID
	 * @param subappVersion 子应用版本
	 * @param args           参数
	 */
	@NewSpan
	@Lock4j(name = "create_permission", keys = {"#appId", "#endpointId", "#subappId", "#subappVersion", "#args.menuId"})
	@BizLog(
		bizId = "permission:create_permission",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createPermission(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated CreatePermissionArgs args) {

		transactionTemplate.executeWithoutResult(status -> {
			try {
				checkParams(mongoTemplate, appId, endpointId, subappId, subappVersion);
				String menuId = Optional.ofNullable(args.getMenuId()).orElse(ROOT_ID);
				Query menuQuery = Query.query(Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.MENU_ID).is(menuId)
				);
				MenuMongodb menu = mongoTemplate.findOne(menuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				if (menu == null && menuId.equals(ROOT_ID)) {
					menu = menuCommonService.createDefaultMenu(appId, endpointId, subappId, subappVersion);
				}
				if (menu == null) {
					throw new ConflictBusinessException("MenuId 错误");
				}
				PermissionMongodb element = PermissionMongodb.builder()
					.appId(appId)
					.endpointId(endpointId)
					.subappId(subappId)
					.subappVersion(subappVersion)
					.menuId(menuId)
					.permissionId(args.getPermissionId())
					.permissionName(args.getPermissionName())
					.icon(args.getIcon())
					.authorities(args.getAuthorities())
					.type(args.getType())
					.defaultPermission(Optional.ofNullable(args.getDefaultPermission()).orElse(false))
					.hiddenPermission(Optional.ofNullable(args.getHiddenPermission()).orElse(false))
					.sort(Optional.ofNullable(args.getSort()).orElse(System.currentTimeMillis()))
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build())
					.build();
				mongoTemplate.insert(element, MongodbConstants.Collection.PERMISSION);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.debug("e", e);
				throw new ConflictBusinessException("创建功能权限失败");
			}
		});
	}


	/**
	 * 功能权限修改
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_permission", keys = {"#appId", "#endpointId", "#subappId", "#subappVersion", "#args.permissionId"})
	@BizLog(
		bizId = "permission:modify_permission",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)

	public void modifyPermission(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated ModifyPermissionArgs args) {
		transactionTemplate.execute(status -> {
			try {
				checkParams(mongoTemplate, appId, endpointId, subappId, subappVersion);
				Criteria criteria = Criteria
					.where(PermissionMongodb.FIELD.APP_ID).is(appId)
					.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(PermissionMongodb.FIELD.PERMISSION_ID).is(args.getPermissionId());
				Query query = Query.query(criteria);
				PermissionMongodb permissionMongodb = mongoTemplate.findOne(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);

				if (permissionMongodb == null) {
					throw new ConflictBusinessException("功能权限不存在，更新失败");
				}

				Optional.ofNullable(args.getMenuId()).ifPresent(permissionMongodb::setMenuId);
				Optional.ofNullable(args.getPermissionName()).ifPresent(permissionMongodb::setPermissionName);
				Optional.ofNullable(args.getAuthorities()).ifPresent(permissionMongodb::setAuthorities);
				Optional.ofNullable(args.getType()).ifPresent(permissionMongodb::setType);
				Optional.ofNullable(args.getDefaultPermission()).ifPresent(permissionMongodb::setDefaultPermission);
				Optional.ofNullable(args.getHiddenPermission()).ifPresent(permissionMongodb::setHiddenPermission);
				Optional.ofNullable(args.getSort()).ifPresent(permissionMongodb::setSort);
				Optional.ofNullable(args.getIcon()).ifPresent(permissionMongodb::setIcon);
				mongoTemplate.save(permissionMongodb, MongodbConstants.Collection.PERMISSION);
				return null;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyPermission", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改功能权限异常");
			}
		});
	}

	/**
	 * 功能权限删除
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param args           args
	 */
	@NewSpan
	@Lock4j(name = "delete_permission", keys = {"#appId", "#endpointId", "#subappId", "#subappVersion"})
	@BizLog(
		bizId = "permission:delete_permission",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void deletePermission(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated DeletePermissionArgs args) {
		List<String> iconList = new ArrayList<>();
		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				checkParams(mongoTemplate, appId, endpointId, subappId, subappVersion);
				Criteria criteria = Criteria
					.where(PermissionMongodb.FIELD.APP_ID).is(appId)
					.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(PermissionMongodb.FIELD.PERMISSION_ID).in(args.getPermissionIds());
				Query query = Query.query(criteria);

				List<PermissionMongodb> removePermissionList = mongoTemplate.findAllAndRemove(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
				if (removePermissionList.isEmpty()) {
					throw new ConflictBusinessException("删除失败");
				}
				iconList.addAll(removePermissionList.stream().map(PermissionMongodb::getIcon).toList());
			} catch (BusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				transactionStatus.setRollbackOnly();
				log.info("e", e);
				throw new ConflictBusinessException("删除功能权限失败");
			}
		});
		//删除图标
		fileCommonService.deletePublicFile(appId.concat("/").concat(FileKeyPrefixConstants.PERMISSION_ICON_PREFIX), iconList);
	}

	/**
	 * 功能权限移动
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param args           args
	 */
	@NewSpan
	@Lock4j(name = "move_permission", keys = {"#appId", "#endpointId", "#subappId", "#subappVersion"})
	@BizLog(
		bizId = "permission:move_permission",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void movePermission(String appId, String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, MovePermissionArgs args) {
		transactionTemplate.execute(status -> {
			try {
				checkParams(mongoTemplate, appId, endpointId, subappId, subappVersion);
				Criteria criteria = Criteria
					.where(PermissionMongodb.FIELD.APP_ID).is(appId)
					.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(PermissionMongodb.FIELD.PERMISSION_ID).is(args.getMovePermissionId());
				Query query = Query.query(criteria);
				PermissionMongodb permissionMongodb = mongoTemplate.findOne(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
				Criteria swapCriteria = Criteria
					.where(PermissionMongodb.FIELD.APP_ID).is(appId)
					.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(PermissionMongodb.FIELD.PERMISSION_ID).is(args.getSwapPermissionId());
				Query swapQuery = Query.query(swapCriteria);
				PermissionMongodb swapPermissionMongodb = mongoTemplate.findOne(swapQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
				if (permissionMongodb == null || swapPermissionMongodb == null) {
					throw new ConflictBusinessException("功能权限不存在，移动失败");
				}
				AtomicLong sort = new AtomicLong();
				sort.set(permissionMongodb.getSort());
				Optional.ofNullable(swapPermissionMongodb.getSort()).ifPresent(permissionMongodb::setSort);
				mongoTemplate.save(permissionMongodb, MongodbConstants.Collection.PERMISSION);

				Optional.of(sort.longValue()).ifPresent(swapPermissionMongodb::setSort);
				mongoTemplate.save(swapPermissionMongodb, MongodbConstants.Collection.PERMISSION);
				return null;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("movePermission", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("功能权限移动异常");
			}
		});
	}

	/**
	 * 检查参数
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 */
	public void checkParams(MongoTemplate mongoTemplate, @Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion) {
		appCommonService.checkAppId(mongoTemplate, appId);
		endpointCommonService.checkEndpointId(mongoTemplate, appId, endpointId);
		subappCommonService.checkSubappId(mongoTemplate, appId, endpointId, subappId);
		subappVersionCommonService.checkSubappVersion(mongoTemplate, subappId, subappVersion);
	}

	public List<MetadataPermission> getMetadataPermissionList(String appId, String endpointId, String subappId, String subappVersion, List<PermissionMongodb> list) {
		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(list.stream().map(PermissionMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds))
			.orElse(Collections.emptyMap());

		Set<String> menuIds = list.stream().map(PermissionMongodb::getMenuId).collect(Collectors.toSet());
		Map<String, PathMenu> pathMenuMap = menuCommonService.getPathMenuMap(appId, endpointId, subappId, subappVersion, menuIds);

		return list.stream().map(x -> PermissionConverter.convertMetadataPermission(x, pathMenuMap, metadataUserMap)).collect(Collectors.toList());
	}

}
