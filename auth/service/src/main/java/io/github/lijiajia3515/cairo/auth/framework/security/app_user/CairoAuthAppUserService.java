package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.PathAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountSnsMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SnsCodeFailedException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoAuthCommonService;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsInfo;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsService;
import io.github.lijiajia3515.cairo.auth.framework.sns.exception.SnsAuthenticationException;
import io.github.lijiajia3515.cairo.auth.modules.app_department.AppDepartmentCommonService;
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

import java.time.Duration;
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
 * 终端用户认证服务类
 */

@Slf4j
@Validated
@Component
public class CairoAuthAppUserService {
	private static final String TOKEN_ID_PREFIX = "endpoint_app_user_";
	protected final MongoTemplate readMongoTemplate;
	protected final RedisTemplate<String, Object> redisTemplate;

	private final CairoAuthCommonService cairoAuthCommonService;
	private final AppDepartmentCommonService departmentCommonService;
	private final SnsService snsService;

	public CairoAuthAppUserService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
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
	 * 根据登录名获取账号信息
	 *
	 * @param appId    应用ID
	 * @param clientId 客户端ID
	 * @return 认证账号信息
	 * @throws AccountNotFoundException 账号不存在异常
	 * @throws AppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthAppUser loadAppUserByUsername(LoginType loginType, String appId, String endpointId, String clientId, String username) {
		checkSystemStatus(appId, endpointId, clientId);

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

		// app user query
		Criteria appUserCriteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
		Query userQuery = Query.query(appUserCriteria);
		userQuery.fields().include(AppUserMongodb.FIELD.USER_ID);

		AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
		if (user == null) {
			user = cairoAuthCommonService.checkAppUserAutoRegister(account, appId, clientId);
			if (user == null) {
				throw new AppUserNotFoundException();
			}
		}
		CairoAuthAppUser appUser = getAuthAppUser(appId, endpointId, clientId, user.getUserId());
		appUser.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		appUser.setLoginType(loginType);

		String encodePassword = getEncodePassword(account.getAccountId());
		appUser.setAccountPassword(encodePassword);
		return appUser;
	}

	/**
	 * 根据登录名获取账号信息
	 *
	 * @param appId         应用id
	 * @param endpointId 终端ID
	 * @param clientId      端id
	 * @param phoneNumber   手机号码
	 * @return 认证账号信息
	 * @throws AccountNotFoundException 账号不存在异常
	 * @throws AppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthAppUser loadAppUserByPhoneNumber(LoginType loginType, String appId, String endpointId, String clientId, String phoneNumber) {
		checkSystemStatus(appId, endpointId, clientId);
		CairoAuthAccount account = null;

		// account criteria
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

		// app user criteria
		Criteria appUserCriteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());

		Query userQuery = Query.query(appUserCriteria);
		userQuery.fields().include(AppUserMongodb.FIELD.USER_ID);
		AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		if (user == null) {
			// 自动注册逻辑
			user = cairoAuthCommonService.checkAppUserAutoRegister(account, appId, clientId);
			if (user == null) {
				throw new AppUserNotFoundException();
			}
		}

		CairoAuthAppUser appUser = getAuthAppUser(appId, endpointId, clientId, user.getUserId());
		appUser.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		appUser.setLoginType(loginType);
		return appUser;
	}

	public CairoAuthAppUser loadAppUserByAccountSns(String appId, String endpointId, String clientId, String snsType, String snsProviderId, String snsCode) {
		SnsInfo snsInfo;
		try {
			snsInfo = snsService.getSnsInfo(snsType, snsProviderId, snsCode);
		} catch (SnsAuthenticationException e) {
			// 转换为security认证异常
			throw new SnsCodeFailedException(e.getMessage());
		}
		// sns转accountId
		String snsPartnerId = snsInfo.getPartnerId();
		String snsPartnerOpenId = snsInfo.getPartnerOpenId();

		Criteria criteria = Criteria
			.where(AccountSnsMongodb.FIELD.SNS_PARTNER_ID).is(snsPartnerId)
			.and(AccountSnsMongodb.FIELD.SNS_PARTNER_OPEN_ID).is(snsPartnerOpenId);

		Query query = Query.query(criteria);
		query.fields().include(AccountSnsMongodb.FIELD.ACCOUNT_ID, AccountSnsMongodb.FIELD.ENABLED);

		AccountSnsMongodb accountSns = readMongoTemplate.findOne(query, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
		CairoAuthAccount account = null;
		if (accountSns != null && accountSns.getEnabled() != null && accountSns.getEnabled()) {
			Query accountQuery = Query.query(Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountSns.getAccountId()));
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
			}
		} else {
			account = cairoAuthCommonService.checkAutoRegisterSns(snsInfo);
		}

		if (account == null) {
			throw new AccountNotFoundException();
		}

		// app user criteria
		Criteria appUserCriteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());

		Query appUserQuery = Query.query(appUserCriteria);
		appUserQuery.fields().include(AppUserMongodb.FIELD.USER_ID);
		AppUserMongodb appUser = readMongoTemplate.findOne(appUserQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		if (appUser == null) {
			// 自动注册逻辑
			appUser = cairoAuthCommonService.checkAppUserAutoRegister(account, appId, clientId);
			if (appUser == null) {
				throw new AppUserNotFoundException("应用用户不存在");
			}
		}

		CairoAuthAppUser user = getAuthAppUser(appId, endpointId, clientId, appUser.getUserId());
		user.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		user.setLoginType(LoginType.SNS);
		user.setSnsType(snsType);
		return user;

	}

	/**
	 * 根据账号id获取认证账号信息
	 *
	 * @param loginType      登录方式
	 * @param appId          应用id
	 * @param endpointId  终端id
	 * @param clientId       客户端ID
	 * @param accountId      账号id
	 * @return 认证账号信息
	 * @throws AccountNotFoundException 账号不存在异常
	 * @throws AppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthAppUser loadAppUserByAccountId(LoginType loginType, String appId, String endpointId, String clientId, String accountId) {
		checkSystemStatus(appId, endpointId, clientId);

		// account
		CairoAuthAccount account;
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

		// app_user
		Criteria app_userCriteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(accountId);

		Query userQuery = Query.query(app_userCriteria);
		userQuery.fields().include(AppUserMongodb.FIELD.USER_ID);
		AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		if (user == null) {
			user = cairoAuthCommonService.checkAppUserAutoRegister(account, appId, clientId);
			if (user == null) {
				throw new AppUserNotFoundException();
			}
		}

		CairoAuthAppUser appUser = getAuthAppUser(appId, endpointId, clientId, user.getUserId());
		appUser.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		appUser.setLoginType(loginType);
		return appUser;
	}

	/**
	 * 根据应用用户id获取认证用户信息
	 *
	 * @param appId    应用id
	 * @param clientId 端id
	 * @param userId   用户id
	 * @return 认证用户信息
	 * @throws AccountNotFoundException 账号不存在异常
	 * @throws AppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthAppUser loadAppUserByAppUserId(LoginType loginType, String appId, String endpointId, String clientId, String userId) {
		checkSystemStatus(appId, endpointId, clientId);
		// app user
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

		CairoAuthAppUser appUser = getAuthAppUser(appId, endpointId, clientId, user.getUserId());
		appUser.setId(TOKEN_ID_PREFIX + CoreConstants.nextIdStr());
		appUser.setLoginType(loginType);
		return appUser;
	}

	/**
	 * 根据用户id获取认证用户信息
	 *
	 * @param appId    应用ID
	 * @param clientId 客户端ID
	 * @param userId   用户ID
	 * @return 认证用户信息
	 * @throws AccountNotFoundException 账号不存在异常
	 * @throws AppUserNotFoundException 用户不存在异常
	 */
	@NewSpan
	public CairoAuthAppUser loadAppUserModel(String appId, String endpointId, String clientId, String userId) {
		checkSystemStatus(appId, endpointId, clientId);
		return getAuthAppUser(appId, endpointId, clientId, userId);
	}

	/**
	 * 获取认证用户
	 *
	 * @param appId         应用ID
	 * @param endpointId 终端ID
	 * @param clientId      客户端ID
	 * @param userId        用户ID
	 * @return 终端认证对象
	 */
	@NewSpan
	public CairoAuthAppUser getAuthAppUser(String appId, String endpointId, String clientId, String userId) {
		// find cache
		CairoAuthAppUser authUser = null;
		try {
			authUser = (CairoAuthAppUser) redisTemplate.opsForValue().get(String.format("%s:%s:%s", CairoAuthRedisConstants.Keys.AUTH_APP_USER, appId, userId));
		} catch (Exception e) {
			redisTemplate.delete(String.format("%s:%s:%s", CairoAuthRedisConstants.Keys.AUTH_APP_USER, appId, userId));
			log.warn("getAuthAppUser error", e);
		}
		if (authUser != null) {
			return authUser;
		}

		// find db
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

		CairoAuthAppUser.CairoAuthAppUserBuilder builder = CairoAuthAppUser.builder();
		builder
			.appId(appId)
			.endpointId(endpointId)
			.clientId(clientId)
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

		Collection<String> authority = getAppUserAuthorityString(appId, endpointId, clientId, user.getUserId());
		builder.authorities(authority.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet()));

		authUser = builder.build();
		// save cache
		redisTemplate.opsForValue().set(String.format("%s:%s:%s:%s", CairoAuthRedisConstants.Keys.AUTH_APP_USER, appId, endpointId, userId), authUser, Duration.ofHours(1));
		return authUser;
	}

	public void removeAppUserCache(String appId, String userId) {
		redisTemplate.delete(String.format("%s:%s:%s", CairoAuthRedisConstants.Keys.AUTH_APP_USER, appId, userId));
	}

	public void removeAllAppUserCache(String appId) {
		redisTemplate.delete(String.format("%s:%s:%s", CairoAuthRedisConstants.Keys.AUTH_APP_USER, appId, "*"));
	}


	/**
	 * 获取用户权限
	 *
	 * @param appId         应用ID
	 * @param endpointId 终端ID
	 * @param userId        用户ID
	 * @return 权限集合
	 */
	@NewSpan
	public Collection<String> getAppUserAuthorityString(String appId, String endpointId, String clientId, String userId) {
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

		List<String> roleIds = new ArrayList<>();
		// 过滤非启用角色
		if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
			Criteria roleCriteria = Criteria
				.where(AppRoleMongodb.FIELD.APP_ID).is(appId)
				.and(AppRoleMongodb.FIELD.ROLE_ID).in(user.getRoleIds())
				.and(AppRoleMongodb.FIELD.ENABLED).is(true);
			Query roleQuery = Query.query(roleCriteria);
			roleQuery.fields().include(AppRoleMongodb.FIELD.ROLE_ID);
			List<String> appUserRoleIds = readMongoTemplate.find(roleQuery, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE).stream()
				.map(AppRoleMongodb::getRoleId).toList();
			roleIds.addAll(appUserRoleIds);
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
	 * @param appId  应用id
	 * @param userId 应用用户id
	 * @return 权限集合
	 */
	@NewSpan
	public Collection<String> getAppUserPermissionIds(String appId, String endpointId, String subappId, String subappVersion, String userId) {
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
					.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId);

				Query roleQuery = Query.query(roleCriteria);
				permissionIds = readMongoTemplate.find(roleQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION)
					.stream().filter(x -> x.getPermissionIds() != null).flatMap(x -> x.getPermissionIds().stream())
					.collect(Collectors.toSet());
			}

			// 默认权限+终端用户权限
			List<Criteria> permissionCriteria = new ArrayList<>(2);
			permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(true));
			if (!permissionIds.isEmpty()) {
				permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.PERMISSION_ID).in(permissionIds));
			}
			Criteria finalPermissionCriteria = Criteria
				.where(PermissionMongodb.FIELD.APP_ID).is(appId)
				.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
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
	 * @param appId         appId
	 * @param endpointId endpointId
	 */
	public void checkSystemStatus(String appId, String endpointId, String clientId) {
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

		//检查客户端状态
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
	}
}
