package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.permission;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.auth.modules.permission.PermissionConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.GetPermissionListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.GetPermissionPageListArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.PathMenu;
import io.github.lijiajia3515.cairo.core.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [tenant_subapp_user/api] action permission service
 */
@Slf4j
@Validated
@Component
public class PermissionTenantSubappApiService {

	private final MongoTemplate readMongoTemplate;
	private final MenuCommonService menuCommonService;

	public PermissionTenantSubappApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													  MenuCommonService menuCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.menuCommonService = menuCommonService;
	}

	/**
	 * 获取元素菜单 集合模式
	 *
	 * @param appId 应用id
	 * @param args  参数
	 * @return 元素 list
	 */
	@NewSpan
	@BizLog(
		bizId = "permission:get_permission_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Permission> getPermissionList(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetPermissionListArgs args) {
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
		return getPermissionList(appId, endpointId, subappId, subappVersion, list);
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
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<Permission> getPermissionPageList(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId,@Valid @NotNull String subappVersion, @Validated GetPermissionPageListArgs args) {
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

		long total = readMongoTemplate.count(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		query.with(args.pageable());
		List<PermissionMongodb> ms = readMongoTemplate.find(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		List<Permission> permissionList = getPermissionList(appId, endpointId, subappId, subappVersion, ms);
		return new Page<>(args, permissionList, total);
	}

	public List<Permission> getPermissionList(String appId, String endpointId, String subappId, String subappVersion, List<PermissionMongodb> list) {
		Set<String> menuIds = list.stream().map(PermissionMongodb::getMenuId).collect(Collectors.toSet());
		Map<String, PathMenu> pathMenuMap = menuCommonService.getPathMenuMap(appId, endpointId, subappId,subappVersion, menuIds);

		return list.stream().map(x -> PermissionConverter.convertPermission(x, pathMenuMap)).collect(Collectors.toList());
	}


}
