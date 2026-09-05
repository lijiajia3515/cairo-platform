package io.github.lijiajia3515.cairo.auth.api.client.subapp_user_authorization;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenExpiredException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.subapp_user.CairoAuthSubappUser;
import io.github.lijiajia3515.cairo.auth.framework.security.subapp_user.CairoAuthSubappUserService;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.SubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.GetSubappUserAuthorizationArgs;
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
 * [client/api] app subapp user authorization service
 */
@Slf4j
@Validated
@Component
public class SubappUserAuthorizationClientApiService {
	private final MongoTemplate readMongoTemplate;
	private final CairoAuthSubappUserService cairoAuthSubappUserService;

	public SubappUserAuthorizationClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													   CairoAuthSubappUserService cairoAuthSubappUserService) {
		this.readMongoTemplate = readMongoTemplate;
		this.cairoAuthSubappUserService = cairoAuthSubappUserService;
	}

	/**
	 * 获取应用级用户认证
	 *
	 * @param args 参数
	 * @return 账号认证模型
	 */
	@NewSpan
	@BizLog(
		bizId = "subapp_user_authorization:get_subapp_user_authorization",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public SubappUserAuthorizationModel getSubappUserAuthorization(@Validated GetSubappUserAuthorizationArgs args) {
		SubappUserAuthorizationModel.SubappUserAuthorizationModelBuilder<?, ?> builder = SubappUserAuthorizationModel.builder();

		Criteria authorizationCriteria = Criteria
			.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppUserAuthorizationMongodb.FIELD.ACCESS_TOKEN.TOKEN_VALUE).is(args.getAccessToken());
		Query authorizationQuery = Query.query(authorizationCriteria);
		AppUserAuthorizationMongodb authorization = readMongoTemplate.findOne(authorizationQuery, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		try {
			if (authorization == null) {
				throw new TokenInvalidException("token错误");
			}
			boolean invalidated = AppUserAuthorizationStatus.isInvalidated(authorization.getStatus());
			if (invalidated) {
				throw new TokenInvalidException("登录失效");
			}

			AppUserAuthorizationMongodb.AccessToken accessToken = authorization.getAccessToken();

			Instant expiresAt = accessToken.getExpiresAt();
			if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
				throw new TokenExpiredException("登录过期");
			}


			CairoAuthSubappUser user = cairoAuthSubappUserService.getAuthSubappUser(authorization.getAppId(), authorization.getEndpointId(), args.getSubappId(), args.getSubappVersion(), authorization.getUserId());

			if (user.isAccountLocked()) {
				throw new LockedException("账号被锁定");
			}

			if (!user.isAccountEnabled()) {
				throw new DisabledException("账号被禁用");
			}

			if (!user.isUserEnabled()) {
				throw new AppUserDisabledException();
			}

			if (!user.isSubappStatus()) {
				throw new SubappNotApplyException();
			}

			return builder.status(DefaultBusiness.SUCCESS.getCode())
				// token信息
				.tokenId(authorization.getTokenId())
				.issuedAt(accessToken.getIssuedAt())
				.expiresAt(accessToken.getExpiresAt())
				.authorizedScopes(authorization.getAuthorizedScopes())
				.appId(authorization.getAppId())
				.endpointId(authorization.getEndpointId())
				.subappId(args.getSubappId())
				.subappVersion(args.getSubappVersion())
				// 登录方式
				.loginType(authorization.getLoginType())
				.snsType(authorization.getSnsType())
				// 用户字段
				.userId(user.getUserId())
				.nickname(user.getNickname())
				.phoneNumber(user.getPhoneNumber())
				.userEnabled(user.isUserEnabled())
				.subappStatus(user.isSubappStatus())
				.appAdmin(user.isAppAdmin())
				.roles(user.getRoles())
				.departments(user.getDepartments())
				.tags(user.getTags())
				.position(user.getPosition())
				// 账号字段
				.accountId(user.getAccountId())
				.accountUsername(user.getAccountUsername())
				.accountPhoneNumber(user.getAccountPhoneNumber())
				.accountEmail(user.getAccountEmail())
				.accountAvatarUrl(user.getAccountAvatarUrl())
				.accountEnabled(user.isAccountEnabled())
				.accountLocked(user.isAccountLocked())
				// 权限
				.authorities(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.build();

		}
		// 授权异常
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

		// 应用异常
		catch (AppNotFoundException e) {
			builder.status(CairoAuthBusiness.APP_NOT_FOUND.getCode()).errorMessage(e.getMessage());
		} catch (AppDisabledException e) {
			builder.status(CairoAuthBusiness.APP_DISABLED.getCode()).errorMessage(e.getMessage());
			// 客户端异常
		}
		// 终端异常
		catch (EndpointNotFoundException e) {
			builder.status(CairoAuthBusiness.ENDPOINT_NOT_FOUND.getCode()).errorMessage(e.getMessage());
		} catch (EndpointDisabledException e) {
			builder.status(CairoAuthBusiness.ENDPOINT_DISABLED.getCode()).errorMessage(e.getMessage());
		}
		// 子应用异常
		catch (SubappNotFoundException e) {
			builder.status(CairoAuthBusiness.SUBAPP_NOT_FOUND.getCode()).errorMessage(e.getMessage());
		} catch (SubappNotApplyException e) {
			builder.status(CairoAuthBusiness.SUBAPP_NOT_APPLY.getCode()).errorMessage(e.getMessage());
		} catch (SubappDisabledException e) {
			builder.status(CairoAuthBusiness.SUBAPP_DISABLED.getCode()).errorMessage(e.getMessage());
		}

		// 用户异常
		catch (AppUserNotFoundException e) {
			builder.status(CairoAuthBusiness.APP_USER_NOT_FOUND.getCode()).errorMessage(e.getMessage());
		} catch (AppUserDisabledException e) {
			builder.status(CairoAuthBusiness.APP_USER_DISABLED.getCode()).errorMessage(e.getMessage());
		}

		// 其他认证异常
		catch (AuthenticationException e) {
			builder.status(CairoAuthBusiness.ERROR.getCode()).errorMessage(e.getMessage());
		}

		return builder.build();

	}
}
