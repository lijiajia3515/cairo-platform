package io.github.lijiajia3515.cairo.auth.api.client.account_authorization;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenExpiredException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.AccountAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.GetAccountAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * [client/api] account authorization service
 */
@Slf4j
@Validated
@Component
public class AccountAuthorizationClientApiService {
	private final MongoTemplate readMongoTemplate;

	private final CairoAuthAccountService cairoAuthAccountService;

	public AccountAuthorizationClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												CairoAuthAccountService cairoAuthAccountService) {
		this.readMongoTemplate = readMongoTemplate;
		this.cairoAuthAccountService = cairoAuthAccountService;
	}

	/**
	 * 获取账号认证模型
	 *
	 * @param args 参数
	 * @return 账号认证模型
	 */
	@NewSpan
	@BizLog(
		bizId = "account_authorization:get_account_authorization",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public AccountAuthorizationModel getAccountAuthorization(@Validated GetAccountAuthorizationArgs args) {
		AccountAuthorizationModel.AccountAuthorizationModelBuilder<?, ?> builder = AccountAuthorizationModel.builder();

		Criteria authorizationCriteria = Criteria.where(AccountAuthorizationMongodb.FIELD.ACCESS_TOKEN.TOKEN_VALUE).is(args.getAccountAccessToken());
		Query authorizationQuery = Query.query(authorizationCriteria);
		AccountAuthorizationMongodb authorization = readMongoTemplate.findOne(authorizationQuery, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
		try {
			if (authorization == null) {
				throw new TokenInvalidException("token错误");
			}
			boolean invalidated = AccountAuthorizationStatus.isInvalidated(authorization.getStatus());
			if (invalidated) {
				throw new TokenInvalidException("登录失效");
			}

			AccountAuthorizationMongodb.AccessToken accessToken = authorization.getAccessToken();
			Instant expiresAt = accessToken.getExpiresAt();
			if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
				throw new TokenExpiredException("登录过期");
			}

			CairoAuthAccount account = cairoAuthAccountService.getAuthAccountModel(authorization.getAppId(), authorization.getClientId(), authorization.getAccountId());

			if (account.isLocked()) {
				throw new LockedException("账号被锁定");
			}

			if (!account.isEnabled()) {
				throw new DisabledException("账号被禁用");
			}

			return builder.status(DefaultBusiness.SUCCESS.getCode())
				// 授权信息
				.tokenId(authorization.getTokenId())
				.issuedAt(accessToken.getIssuedAt())
				.expiresAt(accessToken.getExpiresAt())
				.authorizedScopes(authorization.getAuthorizedScopes())
				.appId(authorization.getAppId())
				.clientId(authorization.getClientId())
				// 登录方式
				.loginType(authorization.getLoginType())
				.snsType(authorization.getSnsType())
				// 账号信息
				.accountId(account.getAccountId())
				.nickname(account.getNickname())
				.username(account.getLoginname())
				.phoneNumber(account.getPhoneNumber())
				.email(account.getEmail())
				.avatarUrl(account.getAvatarUrl())
				.locked(account.isLocked())
				.enabled(account.isEnabled())
				.authorities(account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.build();
		}
		// 鉴权异常
		catch (TokenInvalidException e) {
			builder.status(CairoAuthBusiness.TOKEN_INVALID.getCode()).errorMessage(e.getMessage());
		} catch (TokenExpiredException e) {
			builder.status(CairoAuthBusiness.TOKEN_EXPIRED.getCode()).errorMessage(e.getMessage());
		}
		// 账号异常
		catch (UsernameNotFoundException | AccountNotFoundException e) {
			builder.status(CairoAuthBusiness.ACCOUNT_NOT_FOUND.getCode()).errorMessage(e.getMessage());
		} catch (LockedException e) {
			builder.status(CairoAuthBusiness.ACCOUNT_LOCKED.getCode()).errorMessage(e.getMessage());
		} catch (DisabledException e) {
			builder.status(CairoAuthBusiness.ACCOUNT_DISABLED.getCode()).errorMessage(e.getMessage());
		}
		// 其他认证异常
		catch (AuthenticationException e) {
			builder.status(CairoAuthBusiness.ERROR.getCode()).errorMessage(e.getMessage());
		}
		return builder.build();

	}
}
