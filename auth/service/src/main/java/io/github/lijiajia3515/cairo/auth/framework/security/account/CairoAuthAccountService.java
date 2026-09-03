package io.github.lijiajia3515.cairo.auth.framework.security.account;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountSnsMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SnsCodeFailedException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoAuthCommonService;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsInfo;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsService;
import io.github.lijiajia3515.cairo.auth.modules.sns.exception.SnsTokenException;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账号认证服务类
 */
@Slf4j
@Validated
@Component
public class CairoAuthAccountService implements UserDetailsService {
	private static final String TOKEN_ID_PREFIX = "account_";

	private final MongoTemplate readMongoTemplate;
	private final RedisTemplate<String, Object> redisTemplate;


	private final CairoAuthCommonService cairoAuthCommonService;

	private final SnsService snsService;

	public CairoAuthAccountService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								   RedisTemplate<String, Object> redisTemplate,
								   CairoAuthCommonService cairoAuthCommonService, SnsService snsService) {
		this.readMongoTemplate = readMongoTemplate;
		this.redisTemplate = redisTemplate;
		this.cairoAuthCommonService = cairoAuthCommonService;
		this.snsService = snsService;
	}

	/**
	 * 根据登录名获取账号信息
	 *
	 * @param username 用户名
	 * @return 账号信息
	 * @throws UsernameNotFoundException 用户名不存在异常
	 */
	@Override
	@NewSpan
	public CairoAuthAccount loadUserByUsername(String username) throws UsernameNotFoundException {
		Criteria accountCriteria = new Criteria().orOperator(
			Criteria.where(AccountMongodb.FIELD.USERNAME).is(username),
			Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(username),
			Criteria.where(AccountMongodb.FIELD.EMAIL).is(username)
		);
		Query query = Query.query(accountCriteria).with(Sort.by(Sort.Order.asc(AccountMongodb.FIELD.ACCOUNT_ID)));
		AccountMongodb accountMongodb = readMongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

		if (accountMongodb == null) {
			throw new UsernameNotFoundException("账号不存在");
		}

		Query passwordQuery = Query.query(Criteria
			.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(accountMongodb.getAccountId())
			.and(AccountPasswordMongodb.FIELD.TYPE).is(PasswordType.PASSWORD.getType())
		);
		passwordQuery.limit(1).fields().include(AccountPasswordMongodb.FIELD.PASSWORD);
		String encodePassword = Optional.ofNullable(readMongoTemplate.findOne(passwordQuery, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD))
			.map(AccountPasswordMongodb::getPassword)
			.orElse(null);


		return CairoAuthAccount.builder()
			.id(TOKEN_ID_PREFIX + CoreConstants.nextIdStr())
			.loginType(LoginType.PASSWORD)
			.accountId(accountMongodb.getAccountId())
			.loginname(accountMongodb.getUsername())
			.password(encodePassword)
			.phoneNumber(accountMongodb.getPhoneNumber())
			.nickname(accountMongodb.getNickname())
			.email(accountMongodb.getEmail())
			.enabled(accountMongodb.isEnabled())
			.locked(accountMongodb.isLocked())
			.authorities(getAuthority().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()))
			.build();

	}

	/**
	 * 根据手机号获取账号信息
	 *
	 * @param phoneNumber 手机号
	 * @return 账号信息
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException 账号不存在异常
	 */
	@NewSpan
	public CairoAuthAccount loadAccountByPhoneNumber(LoginType loginType, String phoneNumber) throws AccountNotFoundException {
		CairoAuthAccount account = null;
		Query query = Query.query(Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(phoneNumber));
		AccountMongodb accountMongodb = readMongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

		if (accountMongodb == null) {
			// 自动注册逻辑
			account = cairoAuthCommonService.checkAutoRegisterAccountPhoneNumber(phoneNumber);
			if (account == null) {
				throw new AccountNotFoundException();
			}
			return account;
		}

		return CairoAuthAccount.builder()
			.id(TOKEN_ID_PREFIX + CoreConstants.nextIdStr())
			.loginType(loginType)
			.accountId(accountMongodb.getAccountId())
			.loginname(accountMongodb.getUsername())
			.phoneNumber(accountMongodb.getPhoneNumber())
			.email(accountMongodb.getEmail())
			.nickname(accountMongodb.getNickname())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.enabled(accountMongodb.isEnabled())
			.locked(accountMongodb.isLocked())
			.authorities(getAuthority().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()))
			.build();

	}

	public CairoAuthAccount loadAccountBySnsCode(String snsType, String snsProviderId, String snsCode) {
		// code转info
		SnsInfo snsInfo = null;
		try {
			snsInfo = snsService.getSnsInfo(snsType, snsProviderId, snsCode);
		} catch (SnsTokenException e) {
			// 转换为security认证异常
			throw new SnsCodeFailedException(e.getMessage());
		}

		// openid转accountId
		String snsPartnerId = snsInfo.getPartnerId();
		String snsPartnerOpenId = snsInfo.getPartnerOpenId();

		Criteria criteria = Criteria
			.where(AccountSnsMongodb.FIELD.SNS_PARTNER_ID).is(snsPartnerId)
			.and(AccountSnsMongodb.FIELD.SNS_PARTNER_OPEN_ID).is(snsPartnerOpenId);

		Query query = Query.query(criteria);
		query.fields().include(AccountSnsMongodb.FIELD.ACCOUNT_ID, AccountSnsMongodb.FIELD.ENABLED);

		AccountSnsMongodb accountSns = readMongoTemplate.findOne(query, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
		CairoAuthAccount account = null;

		// 存在绑定记录
		if (accountSns != null && accountSns.getEnabled() != null && accountSns.getEnabled()) {
			account = getAccount(accountSns.getAccountId());
		}
		// 不存在绑定记录进行自动注册
		if (accountSns == null) {
			account = cairoAuthCommonService.checkAutoRegisterSns(snsInfo);
		}

		if (account != null) {
			return CairoAuthAccount.builder()
				.id(TOKEN_ID_PREFIX + CoreConstants.nextIdStr())
				.loginType(LoginType.SNS)
				.snsType(snsType)
				.accountId(account.getAccountId())
				.loginname(account.getUsername())
				.password(null)
				.phoneNumber(account.getPhoneNumber())
				.nickname(account.getNickname())
				.email(account.getEmail())
				.enabled(account.isEnabled())
				.locked(account.isLocked())
				.authorities(getAuthority().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()))
				.build();
		}
		return null;
	}

	/**
	 * 根据手机号获取账号信息
	 *
	 * @param loginType loginType
	 * @param accountId accountId
	 * @return 账号信息
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException 账号不存在异常
	 */
	@NewSpan
	public CairoAuthAccount loadAccountByAccountId(LoginType loginType, String accountId) throws AccountNotFoundException {
		CairoAuthAccount account = getAccount(accountId);

		if (account == null) {
			throw new AccountNotFoundException();
		}

		return CairoAuthAccount.builder()
			.id(TOKEN_ID_PREFIX + CoreConstants.nextIdStr())
			.loginType(loginType)
			.accountId(account.getAccountId())
			.loginname(account.getUsername())
			.phoneNumber(account.getPhoneNumber())
			.email(account.getEmail())
			.nickname(account.getNickname())
			.avatarUrl(account.getAvatarUrl())
			.enabled(account.isEnabled())
			.locked(account.isLocked())
			.authorities(getAuthority().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()))
			.build();
	}

	/**
	 * 根据账号id获取账号信息
	 *
	 * @param appId     appId
	 * @param clientId  clientId
	 * @param accountId 账号id
	 * @return 账号信息
	 * @throws io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException 账号不存在异常
	 */
	@NewSpan
	public CairoAuthAccount getAuthAccountModel(String appId, String clientId, String accountId) {
		CairoAuthAccount account = getAccount(accountId);
		if (account == null) {
			throw new AccountNotFoundException();
		}

		return CairoAuthAccount.builder()
			.accountId(account.getAccountId())
			.loginname(account.getLoginname())
			.phoneNumber(account.getPhoneNumber())
			.avatarUrl(account.getAvatarUrl())
			.nickname(account.getNickname())
			.email(account.getEmail())
			.enabled(account.isEnabled())
			.locked(account.isLocked())
			.authorities(getAuthority().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()))
			.build();
	}

	/**
	 * 获取权限值
	 *
	 * @return 权限集合
	 */
	public Collection<String> getAuthority() {
		Set<String> authority = new HashSet<>();
		authority.add(CairoSecurityConstants.DEFAULT_AUTHORITY);
		return authority;
	}


	private CairoAuthAccount getAccount(String accountId) {
		CairoAuthAccount account = null;

		// find cache
		try {
			account = (CairoAuthAccount) redisTemplate.opsForValue().get(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_ACCOUNT, accountId));
		} catch (Exception e) {
			log.warn("redis get ", e);
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_ACCOUNT, accountId));
		}

		if (account != null) {
			return account;
		}

		// find db
		Query query = Query.query(Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId));
		AccountMongodb accountMongodb = readMongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

		account = CairoAuthAccount.builder()
			.accountId(accountMongodb.getAccountId())
			.loginname(accountMongodb.getUsername())
			.phoneNumber(accountMongodb.getPhoneNumber())
			.email(accountMongodb.getEmail())
			.nickname(accountMongodb.getNickname())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.enabled(accountMongodb.isEnabled())
			.locked(accountMongodb.isLocked())
			.authorities(getAuthority().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()))
			.build();
		redisTemplate.opsForValue().set(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_ACCOUNT, accountId), account, Duration.ofDays(7));
		return account;
	}

	public void removeAccountCache(String accountId) {
		redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_ACCOUNT, accountId));
	}
}
