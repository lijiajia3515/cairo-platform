package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.scope.AccessScope;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantSubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantSubappNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoAuthCommonService;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
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
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROLE_PREFIX;


/**
 * 企业应用级用户认证服务类
 */

@Slf4j
@Validated
@Component
public class CairoAuthTenantAppUserService {
	private static final String TOKEN_ID_PREFIX = "tenant_app_user_";
	protected final MongoTemplate readMongoTemplate;
	protected final RedisTemplate<String, Object> redisTemplate;

	private final CairoAuthCommonService cairoAuthCommonService;
	private final TenantAppDepartmentCommonService tenantAppDepartmentCommonService;

	public CairoAuthTenantAppUserService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												 RedisTemplate<String, Object> redisTemplate,
												 CairoAuthCommonService cairoAuthCommonService, TenantAppDepartmentCommonService tenantAppDepartmentCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.redisTemplate = redisTemplate;
		this.cairoAuthCommonService = cairoAuthCommonService;
		this.tenantAppDepartmentCommonService = tenantAppDepartmentCommonService;
	}

	/**
	 * 根据登录名获取账号信息
	 *
	 * @param appId    应用ID
	 * @param clientId 客户端ID
	 * @param tenantId 企业ID
	 * @param username 用户名
	 * @return 认证账号信息
	 * @throws AccountNotFoundException                                                                 账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthTenantAppUser loadTenantAppUserByUsername(LoginType loginType, String tenantId, String appId, String endpointId, String clientId, String username) {
		checkSystemStatus(tenantId, appId, endpointId, clientId);

		// account
		CairoAuthAccount account = null;
		Criteria accountCriteria = new Criteria()
			.orOperator(
				Criteria.where(AccountMongodb.FIELD.USERNAME).is(username),
				Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(username),
				Criteria.where(AccountMongodb.FIELD.EMAIL).is(username)
			);
		Query accountQuery = Query.query(accountCriteria);
		accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.PHONE_NUMBER, AccountMongodb.FIELD.NICKNAME);
		AccountMongodb accountMongodb = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (accountMongodb == null) {
			throw new AccountNotFoundException();
		}
		account = CairoAuthAccount.builder()
			.accountId(accountMongodb.getAccountId())
			.loginname(accountMongodb.getUsername())
			.phoneNumber(accountMongodb.getPhoneNumber())
			.email(accountMongodb.getEmail())
			.nickname(accountMongodb.getNickname())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.build();

		// user query
		Criteria userCriteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
		Query userQuery = Query.query(userCriteria);
		userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID);

		TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
		if (user == null) {
			user = cairoAuthCommonService.checkTenantAppUserAutoRegister(account, tenantId, appId, clientId);
			if (user == null) {
				throw new TenantAppUserNotFoundException();
			}
		}
		CairoAuthTenantAppUser tenantAppUser = getAuthTenantAppUser(tenantId, appId, endpointId, clientId, user.getUserId());
		tenantAppUser.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		tenantAppUser.setLoginType(loginType);

		String encodePassword = getEncodePassword(account.getAccountId());
		tenantAppUser.setAccountPassword(encodePassword);
		return tenantAppUser;
	}

	/**
	 * 根据登录名获取账号信息
	 *
	 * @param tenantId      企业id
	 * @param appId         应用id
	 * @param endpointId 终端ID
	 * @param clientId      端id
	 * @param phoneNumber   手机号码
	 * @return 认证账号信息
	 * @throws AccountNotFoundException                                                                 账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthTenantAppUser loadTenantAppUserByPhoneNumber(LoginType loginType, String tenantId, String appId, String endpointId, String clientId, String phoneNumber) {
		checkSystemStatus(tenantId, appId, endpointId, clientId);
		CairoAuthAccount account = null;
		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(phoneNumber);
		Query accountQuery = Query.query(accountCriteria);

		accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.PHONE_NUMBER, AccountMongodb.FIELD.NICKNAME);
		AccountMongodb accountMongodb = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (accountMongodb != null) {
			account = CairoAuthAccount.builder()
				.accountId(accountMongodb.getAccountId())
				.loginname(accountMongodb.getUsername())
				.phoneNumber(accountMongodb.getPhoneNumber())
				.email(accountMongodb.getEmail())
				.nickname(accountMongodb.getNickname())
				.avatarUrl(accountMongodb.getAvatarUrl())
				.build();
		} else {
			// 自动注册逻辑
			account = cairoAuthCommonService.checkAutoRegisterAccountPhoneNumber(phoneNumber);
			if (account == null) {
				throw new AccountNotFoundException();
			}
		}

		// user criteria
		Criteria userCriteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());

		Query userQuery = Query.query(userCriteria);
		userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID);
		TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

		if (user == null) {
			// 自动注册逻辑
			user = cairoAuthCommonService.checkTenantAppUserAutoRegister(account, tenantId, appId, clientId);
			if (user == null) {
				throw new TenantAppUserNotFoundException();
			}
		}

		CairoAuthTenantAppUser tenantAppUser = getAuthTenantAppUser(tenantId, appId, endpointId, clientId, user.getUserId());
		tenantAppUser.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		tenantAppUser.setLoginType(loginType);
		return tenantAppUser;
	}

	/**
	 * 根据账号id获取认证账号信息
	 *
	 * @param loginType 登录方式
	 * @param appId     应用id
	 * @param clientId  终端ID
	 * @param tenantId  企业id
	 * @param accountId 账号id
	 * @return 认证账号信息
	 * @throws AccountNotFoundException                                                                 账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthTenantAppUser loadTenantAppUserByAccountId(LoginType loginType, String tenantId, String appId, String endpointId, String clientId, String accountId) {
		checkSystemStatus(tenantId, appId, endpointId, clientId);

		// account
		CairoAuthAccount account = null;
		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId);
		Query accountQuery = Query.query(accountCriteria);

		accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.PHONE_NUMBER, AccountMongodb.FIELD.NICKNAME);

		AccountMongodb accountMongodb = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (accountMongodb == null) {
			throw new AccountNotFoundException();
		}
		account = CairoAuthAccount.builder()
			.accountId(accountMongodb.getAccountId())
			.loginname(accountMongodb.getUsername())
			.phoneNumber(accountMongodb.getPhoneNumber())
			.email(accountMongodb.getEmail())
			.nickname(accountMongodb.getNickname())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.build();

		// user query
		Criteria userCriteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(accountId);

		Query userQuery = Query.query(userCriteria);
		userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID);
		TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

		if (user == null) {
			user = cairoAuthCommonService.checkTenantAppUserAutoRegister(account, tenantId, appId, clientId);
			if (user == null) {
				throw new TenantAppUserNotFoundException();
			}
		}

		CairoAuthTenantAppUser tenantAppUser = getAuthTenantAppUser(tenantId, appId, endpointId, clientId, user.getUserId());
		tenantAppUser.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		tenantAppUser.setLoginType(loginType);
		return tenantAppUser;
	}

	/**
	 * 根据用户id获取认证用户信息
	 *
	 * @param appId    应用id
	 * @param clientId 端id
	 * @param tenantId 企业id
	 * @param userId   用户id
	 * @return 认证用户信息
	 * @throws AccountNotFoundException                                                                 账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthTenantAppUser loadTenantAppUserByUserId(LoginType loginType, String tenantId, String appId, String endpointId, String clientId, String userId) {
		checkSystemStatus(tenantId, appId, endpointId, clientId);
		// user criteria
		Criteria userCriteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);

		Query userQuery = Query.query(userCriteria);
		userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID, TenantAppUserMongodb.FIELD.ACCOUNT_ID);
		TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
		if (user == null) {
			throw new TenantAppUserNotFoundException();
		}

		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(user.getAccountId());
		Query accountQuery = Query.query(accountCriteria);
		accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.NICKNAME);

		AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) {
			throw new AccountNotFoundException();
		}

		CairoAuthTenantAppUser tenantAppUser = getAuthTenantAppUser(tenantId, appId, endpointId, clientId, user.getUserId());
		tenantAppUser.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		tenantAppUser.setLoginType(loginType);
		return tenantAppUser;
	}

	/**
	 * 根据用户id获取认证用户信息
	 *
	 * @param appId    应用id
	 * @param clientId 端id
	 * @param tenantId 企业id
	 * @param userId   账号id
	 * @return 认证用户信息
	 * @throws AccountNotFoundException                                                                 账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthTenantAppUser loadTenantAppUserModel(String tenantId, String appId, String endpointId, String clientId, String userId) {
		checkSystemStatus(tenantId, appId, endpointId, clientId);
		return getAuthTenantAppUser(tenantId, appId, endpointId, clientId, userId);
	}

	/**
	 * 根据用户id获取认证用户信息
	 *
	 * @param appId    应用id
	 * @param clientId 端id
	 * @param tenantId 企业id
	 * @param userId   账号id
	 * @return 认证用户信息
	 * @throws AccountNotFoundException                                                                 账号不存在异常
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthTenantAppUser loadCustomTenantAppUserModel(String tenantId, String appId, String endpointId, String clientId, String subappId, String subappVersion, String userId) {
		checkSystemStatus(tenantId, appId, endpointId, clientId);
		checkSubappStatus(tenantId, appId, endpointId, subappId, subappVersion);
		return getAuthTenantAppUser(tenantId, appId, endpointId, clientId, userId);
	}

	/**
	 * 获取认证用户
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param clientId clientId
	 * @param userId   userId
	 * @return auth user
	 */
	@NewSpan
	private CairoAuthTenantAppUser getAuthTenantAppUser(String tenantId, String appId, String endpointId, String clientId, String userId) {
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

		CairoAuthTenantAppUser.CairoAuthTenantAppUserBuilder builder = CairoAuthTenantAppUser.builder();

		builder
			.appId(appId)
			.endpointId(endpointId)
			.clientId(clientId)

			.tenantId(tenantId)
			.userId(user.getUserId())
			.nickname(user.getNickname())
			.phoneNumber(user.getPhoneNumber())
			.userEnabled(Optional.ofNullable(user.getEnabled()).orElse(false))
			.position(user.getPosition())
			.appAdmin(Optional.ofNullable(user.getAdmin()).orElse(false))
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

		Collection<String> authority = getTenantAppUserAuthorityString(tenantId, appId, endpointId, user.getUserId());
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
	public Collection<String> getTenantAppUserAuthorityString(String tenantId, String appId, String endpointId, String userId) {
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
	public Collection<String> getTenantAppUserPermissionIds(String tenantId, String appId, String endpointId, String userId) {
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

		List<String> roleAuthorities = Optional.ofNullable(user.getRoleIds()).orElse(Collections.emptyList()).stream().map(ROLE_PREFIX::concat).toList();
		permissions.addAll(roleAuthorities);

		if (permissions.isEmpty()) {
			permissions.add(CairoSecurityConstants.DEFAULT_AUTHORITY);
		}

		return permissions.stream().filter(StringUtils::hasText).collect(Collectors.toList());
	}

	@NewSpan
	public String getEncodePassword(String accountId) {
		Query query = Query.query(Criteria
			.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(accountId)
			.and(AccountPasswordMongodb.FIELD.TYPE).is(PasswordType.PASSWORD.getType())
		);
		query.fields().include(AccountPasswordMongodb.FIELD.PASSWORD);
		return Optional.ofNullable(readMongoTemplate.findOne(query, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD))
			.map(AccountPasswordMongodb::getPassword)
			.orElse(null);
	}

	/**
	 * 检查系统状态
	 *
	 * @param tenantId      tenantId
	 * @param appId         appId
	 * @param endpointId endpointId
	 */
	public void checkSystemStatus(String tenantId, String appId, String endpointId, String clientId) {
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

		// 检查客户端状态
		Criteria clientCriteria = Criteria.where(ClientMongodb.FIELD.CLIENT_ID).is(clientId);
		Query clientQuery = Query.query(clientCriteria);
		ClientMongodb clientMongodb = readMongoTemplate.findOne(clientQuery, ClientMongodb.class, MongodbConstants.Collection.CLIENT);

		if (clientMongodb == null) {
			throw new ClientNotFoundException();
		}

		if (clientMongodb.getEnabled() != null && !clientMongodb.getEnabled()) {
			throw new ClientDisabledException();
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
	}

	/**
	 * 检查系统状态
	 *
	 * @param tenantId      tenantId
	 * @param appId         appId
	 * @param endpointId endpointId
	 */
	private void checkSubappStatus(String tenantId, String appId, String endpointId, String subappId, String subappVersion) {
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
