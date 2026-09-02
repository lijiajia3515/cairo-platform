package io.github.lijiajia3515.cairo.auth.api.client.permission;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.auth.modules.permission.PermissionConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.client.permission.GetPermissionListArgs;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.PathMenu;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.modules.subapp_version.SubappVersionCommonService;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
public class PermissionClientApiService {

	private final MongoTemplate readMongoTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final SubappCommonService subappCommonService;
	private final SubappVersionCommonService subappVersionCommonService;
	private final MenuCommonService menuCommonService;


	public PermissionClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
											AppCommonService appCommonService,
											EndpointCommonService endpointCommonService,
											SubappCommonService subappCommonService,
											SubappVersionCommonService subappVersionCommonService,
											MenuCommonService menuCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.subappCommonService = subappCommonService;
		this.subappVersionCommonService = subappVersionCommonService;
		this.menuCommonService = menuCommonService;
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
	public List<Permission> getPermissionList(@Valid @NotNull String appId, @Valid String endpointId, @Valid String subappId, @Valid String subappVersion, @Validated GetPermissionListArgs args) {
		checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
		Criteria criteria = Criteria
			.where(PermissionMongodb.FIELD.APP_ID).is(appId)
			.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);
		Optional.ofNullable(args.getMenuIds()).filter(x -> !x.isEmpty()).ifPresent(menuId -> criteria.and(PermissionMongodb.FIELD.MENU_ID).in(menuId));
		Optional.ofNullable(args.getDefaultPermission()).ifPresent(menuId -> criteria.and(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(args.getDefaultPermission()));
		Optional.ofNullable(args.getPermissionIds()).filter(x -> !x.isEmpty()).ifPresent(menuId -> criteria.and(PermissionMongodb.FIELD.PERMISSION_ID).in(args.getPermissionIds()));

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.by(PermissionMongodb.FIELD.SORT)
		));
		List<PermissionMongodb> list = readMongoTemplate.find(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		return getPermissionList(appId, endpointId, subappId, subappVersion, list);
	}

	/**
	 * 获取我的功能权限 集合模式
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
	public List<Permission> getMyPermissionList(@Valid @NotNull String appId, @Valid String endpointId, @Valid String subappId, @Valid String subappVersion, @Validated GetPermissionListArgs args) {
		checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
		List<Criteria> permissionCriteria = new ArrayList<>(2);
		permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(true));
		if (null != args.getPermissionIds() && !args.getPermissionIds().isEmpty()) {
			permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.PERMISSION_ID).in(args.getPermissionIds()));
		}

		Criteria criteria = Criteria
			.where(PermissionMongodb.FIELD.APP_ID).is(appId)
			.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.orOperator(permissionCriteria);
		Optional.ofNullable(args.getMenuIds()).filter(x -> !x.isEmpty()).ifPresent(menuId -> criteria.and(PermissionMongodb.FIELD.MENU_ID).in(menuId));

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.by(PermissionMongodb.FIELD.SORT)
		));
		List<PermissionMongodb> list = readMongoTemplate.find(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		return getPermissionList(appId, endpointId, subappId, subappVersion, list);
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

	public List<Permission> getPermissionList(String appId, String endpointId, String subappId,String subappVersion, List<PermissionMongodb> list) {
		Set<String> menuIds = list.stream().map(PermissionMongodb::getMenuId).collect(Collectors.toSet());
		Map<String, PathMenu> pathMenuMap = menuCommonService.getPathMenuMap(appId, endpointId, subappId,subappVersion, menuIds);

		return list.stream().map(x -> PermissionConverter.convertPermission(x, pathMenuMap)).collect(Collectors.toList());
	}

}
