package io.github.lijiajia3515.cairo.auth.framework.security.event;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentInfo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.TenantAppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AccountLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AppUserLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.ClientLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.TenantAppUserLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.framework.http.CairoUserAgentUtil;
import io.github.lijiajia3515.cairo.auth.framework.security.account.AccountAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.AppUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.AppUserAuthenticationTokenRequest;
import io.github.lijiajia3515.cairo.auth.framework.security.core.AccountAuthType;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication.SimpleAccountAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication.SimpleAppUserAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication.SimpleClientAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication.SimpleTenantAppUserAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.TenantAppUserAuthenticationTokenConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.TenantAppUserAuthenticationTokenRequest;
import io.github.lijiajia3515.cairo.auth.modules.ip2region.Ip2RegionService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.ACCOUNT;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.APP_USER;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.CLIENT;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.TENANT_APP_USER;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames.ACCESS_TOKEN_FORMAT;

@Slf4j
@Component
public class AuthSuccessLoginLogHandler {

	private final Service service;

	public AuthSuccessLoginLogHandler(Service service, MongoClient mongo) {
		this.service = service;
	}

	/**
	 * 认证成功 事件监听
	 *
	 * @param event 事件
	 */
	@EventListener(AuthenticationSuccessEvent.class)
	public void authSuccessLoginLogHandler(AuthenticationSuccessEvent event) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String clientIP = JakartaServletUtil.getClientIP(request);
		String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("unknown");
		LocalDateTime loginTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault());
		if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
			// 单点 认证成功
			service.updateAuthentication(clientIP, userAgent, loginTime, (UsernamePasswordAuthenticationToken) event.getAuthentication());
		} else if (event.getAuthentication() instanceof OAuth2ClientAuthenticationToken) {
			// oauth2 client 认证成功
			service.updateOAuthClientToken(clientIP, userAgent, loginTime, (OAuth2ClientAuthenticationToken) event.getAuthentication());
		} else if (event.getAuthentication() instanceof OAuth2AccessTokenAuthenticationToken) {
			// oauth2 token 授权成功
			service.updateOAuth2AccessToken(clientIP, userAgent, loginTime, (OAuth2AccessTokenAuthenticationToken) event.getAuthentication());
		} else if (event.getAuthentication() instanceof CairoOAuthClientAuthenticationToken) {
			// oauth client access token 认证成功
			service.updateCairoOAuthClient(clientIP, userAgent, loginTime, (CairoOAuthClientAuthenticationToken) event.getAuthentication());
		} else if (event.getAuthentication() instanceof CairoOAuthAccountAuthenticationToken) {
			// oauth account access token 认证成功
			service.updateCairoOAuthAccount(clientIP, userAgent, loginTime, (CairoOAuthAccountAuthenticationToken) event.getAuthentication());
		} else if (event.getAuthentication() instanceof CairoOAuthAppUserAuthenticationToken) {
			// oauth app user access token 认证成功
			service.updateCairoOAuthAppUser(clientIP, userAgent, loginTime, (CairoOAuthAppUserAuthenticationToken) event.getAuthentication());
		} else if (event.getAuthentication() instanceof CairoOAuthTenantAppUserAuthenticationToken) {
			// oauth endpoint user access token 认证成功
			service.updateCairoOAuthTenantAppUser(clientIP, userAgent, loginTime, (CairoOAuthTenantAppUserAuthenticationToken) event.getAuthentication());
		} else {
			log.warn("login warn unsupported: type: {} principal: {} timestamp: {}", event.getAuthentication().getClass().getName(), event.getAuthentication().getName(), event.getTimestamp());
		}
	}


	// ========== 认证类 ==========

	@Component
	public static class Service {
		private final MongoTemplate mongoTemplate;
		private final JwtDecoder jwtDecoder;
		private final SimpleAccountAuthenticationConverter simpleAccountAuthenticationConverter = new SimpleAccountAuthenticationConverter();
		private final SimpleAppUserAuthenticationConverter simpleAppUserAuthenticationConverter = new SimpleAppUserAuthenticationConverter();
		private final SimpleTenantAppUserAuthenticationConverter simpleTenantAppUserAuthenticationConverter = new SimpleTenantAppUserAuthenticationConverter();
		private final SimpleClientAuthenticationConverter simpleClientAuthenticationConverter = new SimpleClientAuthenticationConverter();
		private final Ip2RegionService ip2RegionService;
		private final AppUserAuthenticationTokenConverter appUserAuthenticationTokenConverter;
		private final AccountAuthenticationTokenConverter accountAuthenticationTokenConverter;
		private final TenantAppUserAuthenticationTokenConverter tenantAppUserAuthenticationTokenConverter;

		public Service(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
					   JwtDecoder jwtDecoder,
					   Ip2RegionService ip2RegionService,
					   AppUserAuthenticationTokenConverter appUserAuthenticationTokenConverter,
					   AccountAuthenticationTokenConverter accountAuthenticationTokenConverter,
					   TenantAppUserAuthenticationTokenConverter tenantAppUserAuthenticationTokenConverter) {
			this.mongoTemplate = mongoTemplate;
			this.jwtDecoder = jwtDecoder;
			this.ip2RegionService = ip2RegionService;
			this.appUserAuthenticationTokenConverter = appUserAuthenticationTokenConverter;
			this.accountAuthenticationTokenConverter = accountAuthenticationTokenConverter;
			this.tenantAppUserAuthenticationTokenConverter = tenantAppUserAuthenticationTokenConverter;
		}

		@Async
		void updateAuthentication(String ip, String userAgent, LocalDateTime loginTime, UsernamePasswordAuthenticationToken token) {
			String logId = CoreConstants.nextIdStr();
			if (token.getPrincipal() instanceof CairoAuthAccount) {
				CairoAuthAccount account = (CairoAuthAccount) token.getPrincipal();
				String accountId = account.getAccountId();
				updateSsoAccountSuccess(logId, accountId, account.getId(), account.getLoginType(), loginTime, ip, userAgent);
			} else if (token.getPrincipal() instanceof CairoAuthAppUser) {
				CairoAuthAppUser authAppUser = (CairoAuthAppUser) token.getPrincipal();
				updateAppUser(logId, authAppUser.getAppId(), authAppUser.getEndpointId(), authAppUser.getClientId(), authAppUser.getUserId(), loginTime, ip, userAgent, authAppUser.getLoginType(), authAppUser.getSnsType(), authAppUser.getId());
			} else if (token.getPrincipal() instanceof CairoAuthTenantAppUser) {
				CairoAuthTenantAppUser tenantAppUser = (CairoAuthTenantAppUser) token.getPrincipal();
				updateTenantAppUser(logId, tenantAppUser.getTenantId(), tenantAppUser.getAppId(), tenantAppUser.getEndpointId(), tenantAppUser.getClientId(), tenantAppUser.getUserId(), loginTime, ip, userAgent, tenantAppUser.getLoginType(), tenantAppUser.getSnsType(), tenantAppUser.getId());
			} else {
				log.warn("ignore token type: type: {} principal: {} timestamp: {}", token.getClass().getName(), token.getName(), loginTime);
			}
		}

		@Async
		void updateOAuthClientToken(String ip, String userAgent, LocalDateTime loginTime, OAuth2ClientAuthenticationToken token) {
			if (token.getRegisteredClient() instanceof CairoRegisteredClient) {
				CairoRegisteredClient registeredClient = (CairoRegisteredClient) token.getRegisteredClient();
				String appId = registeredClient.getAppId();
				String clientId = registeredClient.getClientId();
				updateClient(null, appId, null, clientId, registeredClient.getId(), AuthorizationGrantType.CLIENT_CREDENTIALS, token.getClientAuthenticationMethod().getValue(), loginTime, ip, userAgent);
			} else {
				log.warn("ignore oauth client token: type: {} principal: {} timestamp: {}", token.getClass().getName(), token.getName(), loginTime);
			}
		}

		@Async
		void updateOAuth2AccessToken(String ip, String userAgent, LocalDateTime loginTime, OAuth2AccessTokenAuthenticationToken token) {
			try {
				String format = (String) token.getAdditionalParameters().get(ACCESS_TOKEN_FORMAT);
				if (OAuth2TokenFormat.SELF_CONTAINED.getValue().equals(format)) {
					updateOAuthJwtAccessToken(ip, userAgent, loginTime, token);
				}
				if (OAuth2TokenFormat.REFERENCE.getValue().equals(format)) {
					updateOAuthReferenceAccessToken(ip, userAgent, loginTime, token);
				}

			} catch (RuntimeException e) {
				log.info("updateOAuth2AccessToken", e);
			}
		}

		void updateOAuthJwtAccessToken(String ip, String userAgent, LocalDateTime loginTime, OAuth2AccessTokenAuthenticationToken token) {
			String tokenValue = token.getAccessToken().getTokenValue();
			Jwt jwtToken = jwtDecoder.decode(tokenValue);
			String authType = jwtToken.getClaimAsString(CairoOAuthParameterNames.AUTH_TYPE);
			String logId = CoreConstants.nextIdStr();
			try {
				// 端认证
				if (CLIENT.getValue().equals(authType)) {
					CairoOAuthClientAuthenticationToken clientAuthenticationToken = simpleClientAuthenticationConverter.convert(jwtToken);
					if (clientAuthenticationToken != null) {
						CairoOAuthClientPrincipal clientPrincipal = clientAuthenticationToken.getPrincipal();
						log.debug("login log ignore: type: {} principal: {} timestamp: {}", clientAuthenticationToken.getClass().getName(), token.getName(), loginTime);
						// updateClient(logId, clientPrincipal.getAppId(), null, clientPrincipal.getClientId(), clientPrincipal.getId(), null, clientPrincipal.getLoginType(), loginTime, ip, userAgent);
					}
					return;
				}

				// 账号认证
				if (ACCOUNT.getValue().equals(authType)) {
					CairoOAuthAccountAuthenticationToken accountAuthenticationToken = simpleAccountAuthenticationConverter.convert(jwtToken);
					if (accountAuthenticationToken != null) {
						CairoOAuthAccountPrincipal accountPrincipal = accountAuthenticationToken.getPrincipal();
						updateOAuth2AccountSuccess(logId, accountPrincipal.getAccountId(), accountPrincipal.getLoginType(), accountPrincipal.getSnsType(), accountPrincipal.getAppId(), accountPrincipal.getClientId(), accountPrincipal.getId(), loginTime, ip, userAgent);
					}
					return;
				}
				// 应用级用户认证
				if (APP_USER.getValue().equals(authType)) {
					CairoOAuthAppUserAuthenticationToken appUserAuthenticationToken = simpleAppUserAuthenticationConverter.convert(jwtToken);
					if (appUserAuthenticationToken != null) {
						CairoOAuthAppUserPrincipal appUserPrincipal = appUserAuthenticationToken.getPrincipal();
						updateAppUser(logId, appUserPrincipal.getAppId(), appUserPrincipal.getEndpointId(), appUserPrincipal.getClientId(), appUserPrincipal.getUserId(), loginTime, ip, userAgent, appUserPrincipal.getLoginType(), appUserPrincipal.getSnsType(), appUserPrincipal.getId());
					}
					return;
				}

				// 企业应用级用户认证
				if (TENANT_APP_USER.getValue().equals(authType)) {
					CairoOAuthTenantAppUserAuthenticationToken tenantAppUserAuthenticationToken = simpleTenantAppUserAuthenticationConverter.convert(jwtToken);
					if (tenantAppUserAuthenticationToken != null) {
						CairoOAuthTenantAppUserPrincipal tenantAppUserPrincipal = tenantAppUserAuthenticationToken.getPrincipal();
						updateTenantAppUser(logId, tenantAppUserPrincipal.getTenantId(), tenantAppUserPrincipal.getAppId(), tenantAppUserPrincipal.getEndpointId(), tenantAppUserPrincipal.getClientId(), tenantAppUserPrincipal.getUserId(), loginTime, ip, userAgent, tenantAppUserPrincipal.getLoginType(), tenantAppUserPrincipal.getSnsType(), tenantAppUserPrincipal.getId());
					}
					return;
				}
				log.warn("auth: {} token: {} not support, please upgrade!", authType, tokenValue);
			} catch (RuntimeException e) {
				log.info("updateOAuth2AccessToken", e);
			}
		}

		void updateOAuthReferenceAccessToken(String ip, String userAgent, LocalDateTime loginTime, OAuth2AccessTokenAuthenticationToken token) {
			String tokenValue = token.getAccessToken().getTokenValue();
			OAuth2AccessToken accessToken = token.getAccessToken();
			String logId = CoreConstants.nextIdStr();
			try {
/*				// 端认证
				if (CLIENT.getValue().equals(authType)) {
					CairoOAuthClientAuthenticationToken clientAuthenticationToken = simpleClientAuthenticationConverter.convert(jwtToken);
					if (clientAuthenticationToken != null) {
						CairoOAuthClientPrincipal clientPrincipal = clientAuthenticationToken.getPrincipal();
						log.debug("login log ignore: type: {} principal: {} timestamp: {}", clientAuthenticationToken.getClass().getName(), token.getName(), loginTime);
						// updateClient(logId, clientPrincipal.getAppId(), null, clientPrincipal.getClientId(), clientPrincipal.getId(), null, clientPrincipal.getLoginType(), loginTime, ip, userAgent);
					}
					return;
				}*/

				// 账号认证
				if (accessToken instanceof OAuthAccountAccessToken) {
					Authentication authentication = accountAuthenticationTokenConverter.convert(accessToken.getTokenValue());
					if (authentication instanceof CairoOAuthAccountAuthenticationToken) {
						CairoOAuthAccountPrincipal accountPrincipal = (CairoOAuthAccountPrincipal) authentication.getPrincipal();
						updateOAuth2AccountSuccess(logId, accountPrincipal.getAccountId(), accountPrincipal.getLoginType(), accountPrincipal.getSnsType(), accountPrincipal.getAppId(), accountPrincipal.getClientId(), accountPrincipal.getId(), loginTime, ip, userAgent);
					}
					return;
				}

				// 应用级用户认证
				if (accessToken instanceof OAuthAppUserAccessToken) {
					Authentication authentication = appUserAuthenticationTokenConverter.convert(new AppUserAuthenticationTokenRequest(
							((OAuthAppUserAccessToken) accessToken).getAppId(),
							((OAuthAppUserAccessToken) accessToken).getEndpointId(),
							accessToken.getTokenValue()
						)
					);
					if (authentication instanceof CairoOAuthAppUserAuthenticationToken) {
						CairoOAuthAppUserPrincipal appUserPrincipal = (CairoOAuthAppUserPrincipal) authentication.getPrincipal();
						updateAppUser(logId, appUserPrincipal.getAppId(), appUserPrincipal.getEndpointId(), appUserPrincipal.getClientId(), appUserPrincipal.getUserId(), loginTime, ip, userAgent, appUserPrincipal.getLoginType(), appUserPrincipal.getSnsType(), appUserPrincipal.getId());
					}
					return;
				}

				// 企业应用级用户认证
				if (accessToken instanceof OAuthTenantAppUserAccessToken) {
					Authentication authentication = tenantAppUserAuthenticationTokenConverter.convert(new TenantAppUserAuthenticationTokenRequest(
							((OAuthTenantAppUserAccessToken) accessToken).getTenantId(),
							((OAuthTenantAppUserAccessToken) accessToken).getAppId(),
							((OAuthTenantAppUserAccessToken) accessToken).getEndpointId(),
							accessToken.getTokenValue()
						)
					);
					if (authentication instanceof CairoOAuthTenantAppUserAuthenticationToken) {
						CairoOAuthTenantAppUserPrincipal tenantAppUserPrincipal = (CairoOAuthTenantAppUserPrincipal) authentication.getPrincipal();
						updateTenantAppUser(logId, tenantAppUserPrincipal.getTenantId(), tenantAppUserPrincipal.getAppId(), tenantAppUserPrincipal.getEndpointId(), tenantAppUserPrincipal.getClientId(), tenantAppUserPrincipal.getUserId(), loginTime, ip, userAgent, tenantAppUserPrincipal.getLoginType(), tenantAppUserPrincipal.getSnsType(), tenantAppUserPrincipal.getId());
					}
					return;
				}


				log.warn("auth: {} token: {} not support, please upgrade!", null, tokenValue);
			} catch (RuntimeException e) {
				log.info("updateOAuth2AccessToken", e);
			}
		}

		@Async
		void updateCairoOAuthClient(String ip, String userAgent, LocalDateTime loginTime, CairoOAuthClientAuthenticationToken token) {
			log.debug("login log ignore: type: {} principal: {} timestamp: {}", token.getClass().getName(), token.getName(), loginTime);
		}

		@Async
		void updateCairoOAuthAccount(String ip, String userAgent, LocalDateTime loginTime, CairoOAuthAccountAuthenticationToken token) {
			log.debug("login log ignore: type: {} principal: {} timestamp: {}", token.getClass().getName(), token.getName(), loginTime);
		}

		@Async
		void updateCairoOAuthAppUser(String ip, String userAgent, LocalDateTime loginTime, CairoOAuthAppUserAuthenticationToken token) {
			log.debug("login log ignore: type: {} principal: {} timestamp: {}", token.getClass().getName(), token.getName(), loginTime);
		}

		@Async
		void updateCairoOAuthTenantAppUser(String ip, String userAgent, LocalDateTime loginTime, CairoOAuthTenantAppUserAuthenticationToken token) {
			log.debug("login log ignore: type: {} principal: {} timestamp: {}", token.getClass().getName(), token.getName(), loginTime);
		}


		void updateClient(String logId, String appId, String endpointId, String clientId, String tokenId, AuthorizationGrantType grantType, String loginType, LocalDateTime loginTime, String ip, String userAgent) {
			Query clientQuery = Query.query(Criteria.where(ClientMongodb.FIELD.CLIENT_ID).is(clientId));
			Update clientUpdate = Update.update(ClientMongodb.FIELD.LOGIN_TIME, loginTime);
			clientUpdate.currentDate(ClientMongodb.FIELD.METADATA.UPDATE_TIME);

			mongoTemplate.updateFirst(clientQuery, clientUpdate, MongodbConstants.Collection.CLIENT);

			UserAgent agent = CairoUserAgentUtil.parse(userAgent);
			String region = ip2RegionService.getRegionStr(ip);

			ClientLoginLogMongodb clientLoginLogMongodb = ClientLoginLogMongodb.builder()
				.logId(Optional.ofNullable(logId).orElse(CoreConstants.nextIdStr()))
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.clientTokenId(tokenId)
				.loginTime(loginTime)
				.grantType(Optional.ofNullable(grantType).map(AuthorizationGrantType::getValue).orElse(null))
				.method(loginType)
				.success(true)
				.errMsg("")
				.ip(ip)
				.region(region)
				.agent(userAgent)
				.os(Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.platform(Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null))
				.engine(Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.app(Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.mobile(agent.isMobile())
				.metadata(AppUserMetadataMongodb.builder().build())
				.build();
			mongoTemplate.insert(clientLoginLogMongodb, MongodbConstants.Collection.CLIENT_LOGIN_LOG);
		}

		void updateSsoAccountSuccess(String logId, String accountId, String accountTokenId, LoginType loginType, LocalDateTime loginTime, String ip, String userAgent) {
			Query accountQuery = Query.query(Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId));
			Update accountUpdate = Update.update(AccountMongodb.FIELD.LOGIN_TIME, loginTime);
			accountUpdate.set(AccountMongodb.FIELD.LOCKED, false);
			accountUpdate.set(AccountMongodb.FIELD.LOCKED_TIME, null);
			accountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult accountUpdateResult = mongoTemplate.updateFirst(accountQuery, accountUpdate, MongodbConstants.Collection.ACCOUNT);
			log.debug("update account login success result: {}", accountUpdateResult);

			Query accountPasswordQuery = Query.query(Criteria
				.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(accountId)
				.and(AccountPasswordMongodb.FIELD.TYPE).is(loginType.getValue())
			);
			Update accountPasswordUpdate = new Update();
			accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_COUNT, 0);
			accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_TIME, null);
			accountPasswordUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult accountPasswordUpdateResult = mongoTemplate.updateFirst(accountPasswordQuery, accountPasswordUpdate, MongodbConstants.Collection.ACCOUNT_PASSWORD);
			log.debug("update account password login success result: {}", accountPasswordUpdateResult);

			UserAgent agent = CairoUserAgentUtil.parse(userAgent);
			String region = ip2RegionService.getRegionStr(ip);

			AccountLoginLogMongodb accountLoginLogMongodb = AccountLoginLogMongodb.builder()
				.logId(Optional.ofNullable(logId).orElse(CoreConstants.nextIdStr()))
				.loginTime(loginTime)
				.accountId(accountId)
				.accountTokenId(accountTokenId)
				.authType(AccountAuthType.SSO.getValue())
				.loginType(loginType.getValue())
				.snsType(null)
				.appId(null)
				.clientId(null)
				.success(true)
				.errMsg("")
				.ip(ip)
				.region(region)
				.agent(userAgent)
				.loginTime(loginTime)
				.os(Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.platform(Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null))
				.engine(Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.app(Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.mobile(agent.isMobile())
				.metadata(AppUserMetadataMongodb.builder().build())
				.build();
			mongoTemplate.insert(accountLoginLogMongodb, MongodbConstants.Collection.ACCOUNT_LOGIN_LOG);
		}

		void updateOAuth2AccountSuccess(String logId, String accountId, LoginType loginType, String snsType, String appId, String clientId, String tokenId, LocalDateTime loginTime, String ip, String userAgent) {
			Query accountQuery = Query.query(Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId));
			Update accountUpdate = Update.update(AccountMongodb.FIELD.LOGIN_TIME, loginTime);
			accountUpdate.set(AccountMongodb.FIELD.LOCKED, false);
			accountUpdate.set(AccountMongodb.FIELD.LOCKED_TIME, null);
			accountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult accountUpdateResult = mongoTemplate.updateFirst(accountQuery, accountUpdate, MongodbConstants.Collection.ACCOUNT);
			log.debug("update account login success result: {}", accountUpdateResult);

			Query accountPasswordQuery = Query.query(Criteria
				.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(accountId)
				.and(AccountPasswordMongodb.FIELD.TYPE).is(loginType.getValue())
			);
			Update accountPasswordUpdate = new Update();
			accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_COUNT, 0);
			accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_TIME, null);
			accountPasswordUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult accountPasswordUpdateResult = mongoTemplate.updateFirst(accountPasswordQuery, accountPasswordUpdate, MongodbConstants.Collection.ACCOUNT_PASSWORD);
			log.debug("update account password login success result: {}", accountPasswordUpdateResult);

			String region = ip2RegionService.getRegionStr(ip);

			UserAgent agent = CairoUserAgentUtil.parse(userAgent);

			Criteria criteria = Criteria
				.where(AccountAuthorizationMongodb.FIELD.ACCOUNT_ID).is(accountId)
				.and(AccountAuthorizationMongodb.FIELD.TOKEN_ID).is(tokenId);
			Update update = new Update();
			update.set(AccountAuthorizationMongodb.FIELD.IP, ip);
			update.set(AccountAuthorizationMongodb.FIELD.REGION, region);
			update.set(AccountAuthorizationMongodb.FIELD.AGENT, userAgent);
			update.set(AccountAuthorizationMongodb.FIELD.OS, Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null));
			update.set(AccountAuthorizationMongodb.FIELD.PLATFORM, Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null));
			update.set(AccountAuthorizationMongodb.FIELD.ENGINE, Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null));
			update.set(AccountAuthorizationMongodb.FIELD.APP, Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null));
			update.set(AccountAuthorizationMongodb.FIELD.MOBILE, agent.isMobile());
			mongoTemplate.updateFirst(Query.query(criteria), update, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);

			AccountLoginLogMongodb accountLoginLogMongodb = AccountLoginLogMongodb.builder()
				.logId(Optional.ofNullable(logId).orElse(CoreConstants.nextIdStr()))
				.loginTime(loginTime)
				.accountId(accountId)
				.accountTokenId(tokenId)
				.authType(AccountAuthType.OAUTH2.getValue())
				.appId(appId)
				.clientId(clientId)
				.loginType(loginType.getValue())
				.snsType(snsType)
				.success(true)
				.errMsg("")
				.ip(ip)
				.region(region)
				.agent(userAgent)
				.os(Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.platform(Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null))
				.engine(Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.app(Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.mobile(agent.isMobile())
				.metadata(AppUserMetadataMongodb.builder().build())
				.build();
			mongoTemplate.insert(accountLoginLogMongodb, MongodbConstants.Collection.ACCOUNT_LOGIN_LOG);
		}

		void updateAppUser(String logId, String appId, String endpointId, String clientId, String userId, LocalDateTime loginTime, String ip, String userAgent, LoginType loginType, String snsType, String tokenId) {
			UserAgent agent = CairoUserAgentUtil.parse(userAgent);
			String region = ip2RegionService.getRegionStr(ip);

			Criteria criteria = Criteria
				.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
				.and(AppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
				.and(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(tokenId);
			Update update = new Update();
			update.set(AppUserAuthorizationMongodb.FIELD.IP, ip);
			update.set(AppUserAuthorizationMongodb.FIELD.REGION, region);
			update.set(AppUserAuthorizationMongodb.FIELD.AGENT, userAgent);
			update.set(AppUserAuthorizationMongodb.FIELD.OS, Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null));
			update.set(AppUserAuthorizationMongodb.FIELD.PLATFORM, Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null));
			update.set(AppUserAuthorizationMongodb.FIELD.ENGINE, Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null));
			update.set(AppUserAuthorizationMongodb.FIELD.APP, Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null));
			update.set(AppUserAuthorizationMongodb.FIELD.MOBILE, agent.isMobile());
			mongoTemplate.updateFirst(Query.query(criteria), update, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);


			AppUserLoginLogMongodb appUserLoginLogMongodb = AppUserLoginLogMongodb.builder()
				.logId(Optional.ofNullable(logId).orElse(CoreConstants.nextIdStr()))
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.userId(userId)
				.appUserTokenId(tokenId)
				.loginTime(loginTime)
				.loginType(loginType.getValue())
				.snsType(snsType)
				.success(true)
				.errMsg("")
				.ip(ip)
				.region(region)
				.agent(userAgent)
				.os(Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.platform(Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null))
				.engine(Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.app(Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.mobile(agent.isMobile())
				.metadata(AppUserMetadataMongodb.builder().build())
				.build();
			mongoTemplate.insert(appUserLoginLogMongodb, MongodbConstants.Collection.APP_USER_LOGIN_LOG);
		}

		void updateTenantAppUser(String logId, String tenantId, String appId, String endpointId, String clientId, String userId, LocalDateTime loginTime, String ip, String userAgent, LoginType loginType, String snsType, String tokenId) {
			UserAgent agent = CairoUserAgentUtil.parse(userAgent);
			String region = ip2RegionService.getRegionStr(ip);

			Criteria criteria = Criteria
				.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
				.and(TenantAppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(TenantAppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
				.and(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(tokenId);
			Update update = new Update();
			update.set(TenantAppUserAuthorizationMongodb.FIELD.IP, ip);
			update.set(TenantAppUserAuthorizationMongodb.FIELD.REGION, region);
			update.set(TenantAppUserAuthorizationMongodb.FIELD.AGENT, userAgent);
			update.set(TenantAppUserAuthorizationMongodb.FIELD.OS, Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null));
			update.set(TenantAppUserAuthorizationMongodb.FIELD.PLATFORM, Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null));
			update.set(TenantAppUserAuthorizationMongodb.FIELD.ENGINE, Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null));
			update.set(TenantAppUserAuthorizationMongodb.FIELD.APP, Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null));
			update.set(TenantAppUserAuthorizationMongodb.FIELD.MOBILE, agent.isMobile());
			mongoTemplate.updateFirst(Query.query(criteria), update, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);

			TenantAppUserLoginLogMongodb tenantAppUserLoginLogMongodb = TenantAppUserLoginLogMongodb.builder()
				.logId(Optional.ofNullable(logId).orElse(CoreConstants.nextIdStr()))
				.tenantId(tenantId)
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.userId(userId)
				.tenantAppUserTokenId(tokenId)
				.loginTime(loginTime)
				.loginType(loginType.getValue())
				.snsType(snsType)
				.success(true)
				.errMsg("")
				.ip(ip)
				.region(region)
				.agent(userAgent)
				.os(Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.platform(Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null))
				.engine(Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.app(Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.mobile(agent.isMobile())
				.metadata(AppUserMetadataMongodb.builder().build())
				.build();
			mongoTemplate.insert(tenantAppUserLoginLogMongodb, MongodbConstants.Collection.TENANT_APP_USER_LOGIN_LOG);
		}
	}
}
