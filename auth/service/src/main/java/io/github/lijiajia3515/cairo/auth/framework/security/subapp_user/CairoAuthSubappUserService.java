package io.github.lijiajia3515.cairo.auth.framework.security.subapp_user;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoAuthCommonService;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsService;
import io.github.lijiajia3515.cairo.auth.modules.app_department.AppDepartmentCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.PathAppDepartment;
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
 * 子应用级用户认证服务类
 */

@Slf4j
@Validated
@Component
public class CairoAuthSubappUserService {
	protected final MongoTemplate readMongoTemplate;
	protected final RedisTemplate<String, Object> redisTemplate;

	private final CairoAuthCommonService cairoAuthCommonService;
	private final AppDepartmentCommonService departmentCommonService;
	private final SnsService snsService;

	public CairoAuthSubappUserService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										  RedisTemplate<String, Object> redisTemplate,
										  CairoAuthCommonService cairoAuthCommonService,
										  AppDepartmentCommonService departmentCommonService,
										  SnsService snsService) {
		this.readMongoTemplate = readMongoTemplate;
		this.redisTemplate = redisTemplate;
		this.cairoAuthCommonService = cairoAuthCommonService;
		this.departmentCommonService = departmentCommonService;
		this.snsService = snsService;
	}

	/**
	 * 根据应用级用户id获取认证用户信息
	 *
	 * @param appId          应用ID
	 * @param endpointId  终端ID
	 * @param subappId      子应用ID
	 * @param subappVersion 子应用版本
	 * @param userId         用户ID
	 * @return 认证用户信息
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException 账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthSubappUser loadSubappUserByAppUserId(String appId, String endpointId, String subappId, String subappVersion, String userId) {
		checkSystemStatus(appId, endpointId, subappId, subappVersion);
		// app_user criteria
		Criteria app_userCriteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.USER_ID).is(userId);

		Query userQuery = Query.query(app_userCriteria);
		userQuery.fields().include(AppUserMongodb.FIELD.USER_ID, AppUserMongodb.FIELD.ACCOUNT_ID);
		AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
		if (user == null) {
			throw new AppUserNotFoundException();
		}

		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(user.getAccountId());
		Query accountQuery = Query.query(accountCriteria);
		accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.NICKNAME);

		AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) {
			throw new AccountNotFoundException();
		}

		return getAuthSubappUser(appId, endpointId, subappId, subappVersion, userId);
	}

	/**
	 * 根据用户id获取认证用户信息
	 *
	 * @param appId          应用ID
	 * @param endpointId  终端ID
	 * @param subappId      子应用ID
	 * @param subappVersion 子应用版本
	 * @param userId         用户ID
	 * @return 认证用户信息
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException 账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthSubappUser getSubappUserModel(String appId, String endpointId, String subappId, String subappVersion, String userId) {
		Criteria appUserCriteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.USER_ID).is(userId);

		Query userQuery = Query.query(appUserCriteria);

		boolean exists = readMongoTemplate.exists(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
		if (!exists) {
			throw new AppUserNotFoundException("用户不存在");
		}

		return getAuthSubappUser(appId, endpointId, subappId, subappVersion, userId);
	}

	/**
	 * 获取认证用户
	 *
	 * @param appId  appId
	 * @param userId userId
	 * @return auth userId
	 */
	@NewSpan
	public CairoAuthSubappUser getAuthSubappUser(String appId, String endpointId, String subappId, String subappVersion, String userId) {
		Criteria userCriteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.USER_ID).is(userId);

		Query userQuery = Query.query(userCriteria);
		AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		if (user == null) {
			throw new AppUserNotFoundException();
		}

		// role map
		Map<String, AppRoleMongodb> roleMap = Optional.ofNullable(user.getRoleIds())
			.filter(x -> !x.isEmpty())
			.map(g -> {
				Criteria roleCriteria = Criteria
					.where(AppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(AppRoleMongodb.FIELD.ROLE_ID).in(user.getRoleIds())
					.and(AppRoleMongodb.FIELD.ENABLED).is(true);

				Query roleQuery = Query.query(roleCriteria);
				return readMongoTemplate.find(roleQuery, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE).stream()
					.collect(Collectors.toMap(AppRoleMongodb::getRoleId, x -> x));
			}).orElse(Collections.emptyMap());

		// app role permission
		Criteria rolePermissionCriteria = Criteria
			.where(AppRolePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(AppRolePermissionMongodb.FIELD.ROLE_ID).in(user.getRoleIds());

		Query rolePermissionQuery = Query.query(rolePermissionCriteria);
		boolean subappStatus = readMongoTemplate.exists(rolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);

		// department map
		Map<String, PathAppDepartment> departmentMap = Optional.ofNullable(user.getDepartmentIds())
			.filter(x -> !x.isEmpty())
			.map(g -> departmentCommonService.getPathAppDepartmentMap(appId, g))
			.orElse(Collections.emptyMap());

		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(user.getAccountId());
		Query accountQuery = Query.query(accountCriteria);
		AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) {
			throw new AccountNotFoundException();
		}

		CairoAuthSubappUser.CairoAuthSubappUserBuilder builder = CairoAuthSubappUser.builder();
		builder
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
				.stream().map(g -> CairoRole.builder().roleId(g).roleName(Optional.ofNullable(roleMap.get(g)).map(AppRoleMongodb::getRoleName).orElse(g)).build())
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

		Collection<String> authority = getSubappUserAuthorityString(appId, endpointId, subappId, subappVersion, user.getUserId());
		builder.authorities(authority.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet()));

		return builder.build();
	}


	/**
	 * 获取账号权限
	 *
	 * @param appId          应用ID
	 * @param endpointId  终端ID
	 * @param subappId      子应用ID
	 * @param subappVersion 子应用版本
	 * @param userId         用户ID
	 * @return 权限集合
	 */
	@NewSpan
	public Collection<String> getSubappUserAuthorityString(String appId, String endpointId, String subappId, String subappVersion, String userId) {
		Criteria app_userCriteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.USER_ID).is(userId);
		Query userQuery = Query.query(app_userCriteria);
		AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		if (user == null) {
			return Collections.singletonList(CairoSecurityConstants.DEFAULT_AUTHORITY);
		}

		List<String> authorities = new ArrayList<>();

		// 系统管理员
		boolean appAdmin = user.getAdmin() != null && user.getAdmin();
		if (appAdmin) {
			authorities.add(CairoSecurityConstants.APP_ADMIN_AUTHORITY);
		}

		boolean subappStatus = false;

		List<String> roleIds = new ArrayList<>();
		// 过滤非启用角色
		if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
			Criteria roleCriteria = Criteria
				.where(AppRoleMongodb.FIELD.APP_ID).is(appId)
				.and(AppRoleMongodb.FIELD.ROLE_ID).in(user.getRoleIds())
				.and(AppRoleMongodb.FIELD.ENABLED).is(true);
			Query roleQuery = Query.query(roleCriteria);
			roleQuery.fields().include(AppRoleMongodb.FIELD.ROLE_ID);
			List<String> app_userRoleIds = readMongoTemplate.find(roleQuery, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE).stream()
				.map(AppRoleMongodb::getRoleId).toList();
			roleIds.addAll(app_userRoleIds);
		}

		// 角色权限值
		List<String> roleAuthorities = roleIds.stream().map(ROLE_PREFIX::concat).toList();
		authorities.addAll(roleAuthorities);

		// 非管理员查询接口权限值
		if (!appAdmin) {
			Set<String> permissionIds = Collections.emptySet();
			if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
				Criteria roleCriteria = Criteria
					.where(AppRolePermissionMongodb.FIELD.APP_ID).is(appId)
					.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(AppRolePermissionMongodb.FIELD.ROLE_ID).in(roleIds);

				Query roleQuery = Query.query(roleCriteria);
				permissionIds = readMongoTemplate.find(roleQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION)
					.stream().filter(x -> x.getPermissionIds() != null).flatMap(x -> x.getPermissionIds().stream())
					.collect(Collectors.toSet());
			}

			// 默认权限+应用级用户权限
			List<Criteria> permissionCriteria = new ArrayList<>(2);
			permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(true));
			if (!permissionIds.isEmpty()) {
				permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.PERMISSION_ID).in(permissionIds));
			}
			Criteria finalPermissionCriteria = Criteria
				.where(PermissionMongodb.FIELD.APP_ID).is(appId)
				.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
				.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
				.orOperator(permissionCriteria);
			Query permissionQuery = Query.query(finalPermissionCriteria);
			List<String> appUserAuthorityList = readMongoTemplate.find(permissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION)
				.stream().map(PermissionMongodb::getAuthorities)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.distinct()
				.toList();
			authorities.addAll(appUserAuthorityList);
		}

		// 无权限添加默认权限
		if (authorities.isEmpty()) {
			authorities.add(CairoSecurityConstants.DEFAULT_AUTHORITY);
		}

		return authorities.stream().filter(StringUtils::hasText).collect(Collectors.toList());
	}

	/**
	 * 获取账号权限
	 *
	 * @param appId  应用id
	 * @param userId 应用级用户id
	 * @return 权限集合
	 */
	@NewSpan
	public Collection<String> getSubappUserPermissionIds(String appId, String endpointId, String subappId, String subappVersion, String userId) {
		Criteria app_userCriteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.USER_ID).is(userId);
		Query userQuery = Query.query(app_userCriteria);
		AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		if (user == null) {
			return Collections.singletonList(CairoSecurityConstants.DEFAULT_AUTHORITY);
		}


		List<String> permissions = new ArrayList<>();

		boolean appAdmin = user.getAdmin() != null && user.getAdmin();
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
					.where(AppRolePermissionMongodb.FIELD.APP_ID).is(appId)
					.and(AppRolePermissionMongodb.FIELD.ROLE_ID).in(user.getRoleIds())
					.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

				Query roleQuery = Query.query(roleCriteria);
				permissionIds = readMongoTemplate.find(roleQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION)
					.stream().filter(x -> x.getPermissionIds() != null).flatMap(x -> x.getPermissionIds().stream())
					.collect(Collectors.toSet());
			}

			// 默认权限+应用级用户权限
			List<Criteria> permissionCriteria = new ArrayList<>(2);
			permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(true));
			if (!permissionIds.isEmpty()) {
				permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.PERMISSION_ID).in(permissionIds));
			}
			Criteria finalPermissionCriteria = Criteria
				.where(PermissionMongodb.FIELD.APP_ID).is(appId)
				.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
				.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
				.orOperator(permissionCriteria);
			Query permissionQuery = Query.query(finalPermissionCriteria);
			List<String> AppUserPermissionIds = readMongoTemplate.find(permissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION)
				.stream().map(PermissionMongodb::getPermissionId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
			permissions.addAll(AppUserPermissionIds);
		}

		if (permissions.isEmpty()) {
			permissions.add(CairoSecurityConstants.DEFAULT_AUTHORITY);
		}

		return permissions.stream().filter(StringUtils::hasText).collect(Collectors.toList());
	}

	/**
	 * 检查系统状态
	 *
	 * @param appId         appId
	 * @param endpointId endpointId
	 */
	public void checkSystemStatus(String appId, String endpointId, String subappId, String subappVersion) {
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
	}
}
