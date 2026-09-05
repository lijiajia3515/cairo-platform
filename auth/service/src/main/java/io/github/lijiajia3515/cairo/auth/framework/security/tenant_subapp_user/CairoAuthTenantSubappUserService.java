package io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantSubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantSubappNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantNotFoundException;
import io.github.lijiajia3515.cairo.auth.domain.dto.scope.AccessScope;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.TenantAppDepartmentCommonService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROLE_PREFIX;


/**
 * 企业应用级用户认证服务类
 */

@Slf4j
@Validated
@Component
public class CairoAuthTenantSubappUserService {
	private static final String TOKEN_ID_PREFIX = "tenant_subapp_user_";
	protected final MongoTemplate readMongoTemplate;
	protected final RedisTemplate<String, Object> redisTemplate;

	private final TenantAppDepartmentCommonService tenantAppDepartmentCommonService;

	public CairoAuthTenantSubappUserService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												RedisTemplate<String, Object> redisTemplate,
												TenantAppDepartmentCommonService tenantAppDepartmentCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.redisTemplate = redisTemplate;
		this.tenantAppDepartmentCommonService = tenantAppDepartmentCommonService;
	}

	/**
	 * 根据用户id获取认证用户信息
	 *
	 * @param tenantId       企业ID
	 * @param appId          应用ID
	 * @param endpointId  终端ID
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param userId         用户ID
	 * @return 认证用户信息
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException       账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthTenantSubappUser loadTenantSubappUserByUserId(String tenantId,
																		  String appId, String endpointId,
																		  String subappId, String subappVersion,
																		  String userId) {
		checkSystemStatus(tenantId, appId, endpointId, subappId, subappVersion);
		// user criteria
		Criteria userCriteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);

		Query userQuery = Query.query(userCriteria);
		userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID, TenantAppUserMongodb.FIELD.ACCOUNT_ID);
		TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
		if (user == null) {
			throw new TenantAppUserNotFoundException("用户不存在");
		}

		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(user.getAccountId());
		Query accountQuery = Query.query(accountCriteria);
		accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.NICKNAME);

		AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) {
			throw new AccountNotFoundException();
		}

		CairoAuthTenantSubappUser tenantSubappUser = getAuthTenantSubappUser(tenantId, appId, endpointId, subappId, subappVersion, user.getUserId());
		tenantSubappUser.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		return tenantSubappUser;
	}

	/**
	 * 根据用户id获取认证用户信息
	 *
	 * @param tenantId       企业ID
	 * @param appId          应用ID
	 * @param endpointId  终端ID
	 * @param subappId      子应用ID
	 * @param subappVersion 子应用版本
	 * @param userId         用户ID
	 * @return 认证用户信息
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException       账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthTenantSubappUser loadTenantSubappUserModel(String tenantId,
																	   String appId, String endpointId,
																	   String subappId, String subappVersion,
																	   String userId) {
		checkSystemStatus(tenantId, appId, endpointId, subappId, subappVersion);
		return getAuthTenantSubappUser(tenantId, appId, endpointId, subappId, subappVersion, userId);
	}

	/**
	 * 获取认证用户
	 *
	 * @param tenantId       tenantId
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param userId         userId
	 * @return auth user
	 */
	@NewSpan
	private CairoAuthTenantSubappUser getAuthTenantSubappUser(String tenantId,
																	  String appId, String endpointId,
																	  String subappId, String subappVersion,
																	  String userId) {
		Criteria userCriteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);

		Query userQuery = Query.query(userCriteria);
		TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

		if (user == null) {
			throw new TenantAppUserNotFoundException();
		}

		// role map
		Map<String, TenantAppRoleMongodb> roleMap = Optional.ofNullable(user.getRoleIds())
			.filter(x -> !x.isEmpty())
			.map(g -> {
				Criteria roleCriteria = Criteria
					.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleMongodb.FIELD.ROLE_ID).in(user.getRoleIds())
					.and(TenantAppRoleMongodb.FIELD.ENABLED).is(true);

				Query roleQuery = Query.query(roleCriteria);
				return readMongoTemplate.find(roleQuery, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE).stream()
					.collect(Collectors.toMap(TenantAppRoleMongodb::getRoleId, x -> x));
			}).orElse(Collections.emptyMap());

		// app role permission
		Criteria rolePermissionCriteria = Criteria
			.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).in(user.getRoleIds());

		Query rolePermissionQuery = Query.query(rolePermissionCriteria);
		boolean subappStatus = readMongoTemplate.exists(rolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);

		// department map
		Map<String, PathTenantAppDepartment> departmentMap = Optional.ofNullable(user.getDepartmentIds())
			.filter(x -> !x.isEmpty())
			.map(g -> tenantAppDepartmentCommonService.getPathDepartmentMap(tenantId, appId, g))
			.orElse(Collections.emptyMap());

		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(user.getAccountId());
		Query accountQuery = Query.query(accountCriteria);
		AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

		if (account == null) {
			throw new AccountNotFoundException();
		}

		CairoAuthTenantSubappUser.CairoAuthTenantSubappUserBuilder builder = CairoAuthTenantSubappUser.builder();

		builder
			.tenantId(tenantId)
			.appId(appId)
			.endpointId(endpointId)
			.subappId(subappId)
			.subappVersion(subappVersion)
			.userId(user.getUserId())
			.nickname(user.getNickname())
			.phoneNumber(user.getPhoneNumber())
			.userEnabled(Optional.ofNullable(user.getEnabled()).orElse(false))
			.subappStatus(user.getAdmin() || subappStatus)
			.appAdmin(Optional.ofNullable(user.getAdmin()).orElse(false))
			.position(user.getPosition())
			.departments(Optional.ofNullable(user.getDepartmentIds()).orElse(Collections.emptyList())
				.stream()
				.map(departmentMap::get)
				.filter(Objects::nonNull)
				.map(g -> CairoDepartment.builder().departmentIds(g.getDepartmentIds()).departmentNames(g.getDepartmentNames()).depth(g.getDepth()).build())
				.collect(Collectors.toList()))
			.roles(Optional.ofNullable(user.getRoleIds()).orElse(Collections.emptyList())
				.stream().map(g -> CairoRole.builder().roleId(g).roleName(Optional.ofNullable(roleMap.get(g)).map(TenantAppRoleMongodb::getRoleName).orElse(g)).build())
				.collect(Collectors.toList())
			)
			.tags(Optional.ofNullable(user.getTagIds()).orElse(Collections.emptyList())
				.stream().map(g -> CairoTag.builder().tagId(g).tagName(g).build())
				.collect(Collectors.toList())
			)

			.accountId(account.getAccountId())
			.accountNickname(account.getNickname())
			.accountUsername(account.getUsername())
			.accountPhoneNumber(account.getPhoneNumber())
			.accountEmail(account.getEmail())
			.accountAvatarUrl(account.getAvatarUrl())
			.accountEnabled(account.isEnabled())
			.accountLocked(account.isLocked())
		;

		Collection<String> authority = getTenantSubappUserAuthorityString(tenantId, appId, endpointId, subappId, subappVersion, user.getUserId());
		builder.authorities(authority.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet()));

		return builder.build();
	}


	/**
	 * 获取账号权限
	 *
	 * @param tenantId      企业id
	 * @param appId         应用id
	 * @param endpointId 终端ID
	 * @param userId        账号id
	 * @return 权限集合
	 */
	@NewSpan
	public Collection<String> getTenantSubappUserAuthorityString(String tenantId,
																	 String appId, String endpointId,
																	 String subappId, String subappVersion,
																	 String userId) {
		Criteria userCriteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);
		Query userQuery = Query.query(userCriteria);
		TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

		if (user == null) {
			return Collections.singletonList(CairoSecurityConstants.DEFAULT_AUTHORITY);
		}

		boolean appAdmin = user.getAdmin() != null && user.getAdmin();

		List<String> authorities = new ArrayList<>();

		// 系统管理员
		if (appAdmin) {
			authorities.add(CairoSecurityConstants.APP_ADMIN_AUTHORITY);
		}

		List<String> roleIds = new ArrayList<>();
		// 过滤非启用角色
		if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
			Criteria roleCriteria = Criteria
				.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
				.and(TenantAppRoleMongodb.FIELD.ROLE_ID).in(user.getRoleIds())
				.and(TenantAppRoleMongodb.FIELD.ENABLED).is(true);
			Query roleQuery = Query.query(roleCriteria);
			roleQuery.fields().include(TenantAppRoleMongodb.FIELD.ROLE_ID);
			List<String> userRoleIds = readMongoTemplate.find(roleQuery, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE).stream()
				.map(TenantAppRoleMongodb::getRoleId).toList();
			roleIds.addAll(userRoleIds);
		}

		// 角色权限值
		List<String> roleAuthorities = roleIds.stream().map(ROLE_PREFIX::concat).toList();
		authorities.addAll(roleAuthorities);

		// 非管理员查询接口权限值
			Set<String> permissionIds = Collections.emptySet();
			if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
				Criteria roleCriteria = Criteria
					.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).in(roleIds);

				Query roleQuery = Query.query(roleCriteria);
				permissionIds = readMongoTemplate.find(roleQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION)
					.stream().filter(x -> x.getPermissionIds() != null).flatMap(x -> x.getPermissionIds().stream())
					.collect(Collectors.toSet());
			}

			// 默认权限+企业子应用级用户权限
			List<Criteria> permissionCriteria = new ArrayList<>(2);
			permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(true));
			if (!permissionIds.isEmpty()) {
				permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.PERMISSION_ID).in(permissionIds));
			}
			Criteria finalPermissionCriteria = Criteria
				.where(PermissionMongodb.FIELD.APP_ID).is(appId)
				.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
				.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
				.orOperator(permissionCriteria);
			Query permissionQuery = Query.query(finalPermissionCriteria);
			List<String> userAuthorityList = readMongoTemplate.find(permissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION)
				.stream().map(PermissionMongodb::getAuthorities)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.distinct()
				.toList();
			authorities.addAll(userAuthorityList);


		// 无权限添加默认权限
		if (authorities.isEmpty()) {
			authorities.add(CairoSecurityConstants.DEFAULT_AUTHORITY);
		}

		return authorities.stream().filter(StringUtils::hasText).collect(Collectors.toList());
	}

	/**
	 * 获取账号权限
	 *
	 * @param appId    应用id
	 * @param tenantId 企业id
	 * @param userId   账号id
	 * @return 权限集合
	 */
	@NewSpan
	public Collection<String> getTenantSubappUserPermissionIds(String tenantId, String appId, String endpointId, String subappId, String subappVersion, String userId) {
		Criteria userCriteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);
		Query userQuery = Query.query(userCriteria);
		TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

		if (user == null) {
			return Collections.singletonList(CairoSecurityConstants.DEFAULT_AUTHORITY);
		}

		boolean appAdmin = user.getAdmin() != null && user.getAdmin();
		List<String> permissions = new ArrayList<>();
		if (appAdmin) {
			permissions.add(CairoSecurityConstants.APP_ADMIN_AUTHORITY);
		}

		List<String> roleAuthorities = Optional.ofNullable(user.getRoleIds()).orElse(Collections.emptyList()).stream().map(ROLE_PREFIX::concat).collect(Collectors.toList());
		permissions.addAll(roleAuthorities);

		// 非管理员查询接口权限值
		if (!appAdmin) {
			Set<String> permissionIds = Collections.emptySet();
			if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
				Criteria roleCriteria = Criteria
					.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).in(user.getRoleIds())
					.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

				Query roleQuery = Query.query(roleCriteria);
				permissionIds = readMongoTemplate.find(roleQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION)
					.stream().filter(x -> x.getPermissionIds() != null).flatMap(x -> x.getPermissionIds().stream())
					.collect(Collectors.toSet());
			}

			// 默认权限+企业子应用级用户权限
			List<Criteria> permissionCriteria = new ArrayList<>(2);
			permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(true));
			if (!permissionIds.isEmpty()) {
				permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.PERMISSION_ID).in(permissionIds));
			}
			Criteria finalPermissionCriteria = Criteria
				.where(PermissionMongodb.FIELD.APP_ID).is(appId)
				.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
				.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
				.orOperator(permissionCriteria);
			Query permissionQuery = Query.query(finalPermissionCriteria);
			List<String> tenantSubappUserPermissionIds = readMongoTemplate.find(permissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION)
				.stream().map(PermissionMongodb::getPermissionId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
			permissions.addAll(tenantSubappUserPermissionIds);
		}

		if (permissions.isEmpty()) {
			permissions.add(CairoSecurityConstants.DEFAULT_AUTHORITY);
		}

		return permissions.stream().filter(StringUtils::hasText).collect(Collectors.toList());
	}

	/**
	 * 检查系统状态
	 *
	 * @param tenantId      tenantId
	 * @param appId         appId
	 * @param endpointId endpointId
	 */
	// 包级可见：同包测试直接覆盖准入闸口矩阵
	void checkSystemStatus(String tenantId, String appId, String endpointId, String subappId, String subappVersion) {
		// 检查应用状态
		Criteria appCriteria = Criteria.where(AppMongodb.FIELD.APP_ID).is(appId);
		Query appQuery = Query.query(appCriteria);
		AppMongodb appMongodb = readMongoTemplate.findOne(appQuery, AppMongodb.class, MongodbConstants.Collection.APP);

		if (appMongodb == null) {
			throw new AppNotFoundException();
		}

		if (appMongodb.getEnabled() != null && !appMongodb.getEnabled()) {
			throw new AppDisabledException();
		}

		// 检查终端状态
		Criteria endpointCriteria = Criteria
			.where(EndpointMongodb.FIELD.APP_ID).is(appId)
			.and(EndpointMongodb.FIELD.ENDPOINT_ID).is(endpointId);
		Query endpointQuery = Query.query(endpointCriteria);
		EndpointMongodb endpointMongodb = readMongoTemplate.findOne(endpointQuery, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);

		if (endpointMongodb == null) {
			throw new EndpointNotFoundException();
		}

		if (endpointMongodb.getEnabled() != null && !endpointMongodb.getEnabled()) {
			throw new EndpointDisabledException();
		}

		// 检查子应用状态
		Criteria subappCriteria = Criteria
			.where(SubappMongodb.FIELD.APP_ID).is(appId)
			.and(SubappMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(SubappMongodb.FIELD.SUBAPP_ID).is(subappId);
		Query subappQuery = Query.query(subappCriteria);
		SubappMongodb subappMongodb = readMongoTemplate.findOne(subappQuery, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);

		if (subappMongodb == null) {
			throw new SubappNotFoundException();
		}

		if (subappMongodb.getEnabled() != null && !subappMongodb.getEnabled()) {
			throw new SubappDisabledException();
		}


		// 检查企业状态
		Criteria tenantCriteria = Criteria.where(TenantMongodb.FIELD.TENANT_ID).is(tenantId);
		Query tenantQuery = Query.query(tenantCriteria);
		TenantMongodb tenantMongodb = readMongoTemplate.findOne(tenantQuery, TenantMongodb.class, MongodbConstants.Collection.TENANT);
		if (tenantMongodb == null) {
			throw new TenantNotFoundException();
		}

		if (tenantMongodb.getEnabled() != null && !tenantMongodb.getEnabled()) {
			throw new TenantDisabledException();
		}

		// 检查企业应用状态
		Criteria tenantAppCriteria = Criteria
			.where(TenantAppMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppMongodb.FIELD.APP_ID).is(appId);
		Query tenantAppQuery = Query.query(tenantAppCriteria);
		TenantAppMongodb tenantAppMongodb = readMongoTemplate.findOne(tenantAppQuery, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);

		if (tenantAppMongodb == null) {
			throw new TenantAppNotApplyException();
		}

		if (tenantAppMongodb.getEnabled() != null && !tenantAppMongodb.getEnabled()) {
			throw new TenantAppDisabledException();
		}

		// 检查企业终端状态
		// 平台级终端企业不可用；企业级需企业开通；开放直通
		if (AccessScope.APP.getScopeValue().equals(endpointMongodb.getScope())) {
			throw new TenantEndpointNotApplyException();
		}
		if (endpointMongodb.getScope() == null || !AccessScope.PUBLIC.getScopeValue().equals(endpointMongodb.getScope())) {
			// 检查企业申请终端的状态
			Criteria tenantEndpointCriteria = Criteria
				.where(TenantEndpointMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(TenantEndpointMongodb.FIELD.APP_ID).is(appId)
				.and(TenantEndpointMongodb.FIELD.ENDPOINT_ID).is(endpointId);

			Query tenantEndpointQuery = Query.query(tenantEndpointCriteria);
			TenantEndpointMongodb tenantEndpointMongodb = readMongoTemplate.findOne(tenantEndpointQuery, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);

			if (tenantEndpointMongodb == null) {
				throw new TenantEndpointNotApplyException();
			}

			if (tenantEndpointMongodb.getEnabled() != null && !tenantEndpointMongodb.getEnabled()) {
				throw new TenantEndpointDisabledException();
			}
		}

		// 检查企业子应用开通状态
		// 平台级子应用企业不可用；企业级需企业按模块开通；缺省/public 随终端开通自动可用
		if (AccessScope.APP.getScopeValue().equals(subappMongodb.getScope())) {
			throw new TenantSubappNotApplyException();
		}
		if (AccessScope.TENANT.getScopeValue().equals(subappMongodb.getScope())) {
			Criteria tenantSubappCriteria = Criteria
				.where(TenantSubappMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(TenantSubappMongodb.FIELD.APP_ID).is(appId)
				.and(TenantSubappMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(TenantSubappMongodb.FIELD.SUBAPP_ID).is(subappId);

			Query tenantSubappQuery = Query.query(tenantSubappCriteria);
			TenantSubappMongodb tenantSubappMongodb = readMongoTemplate.findOne(tenantSubappQuery, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);

			if (tenantSubappMongodb == null) {
				throw new TenantSubappNotApplyException();
			}

			if (tenantSubappMongodb.getEnabled() != null && !tenantSubappMongodb.getEnabled()) {
				throw new TenantSubappDisabledException();
			}
		}
	}
}
