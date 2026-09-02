package io.github.lijiajia3515.cairo.auth.framework.security.event;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AccountLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AppUserLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.ClientLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.TenantAppUserLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.framework.http.CairoUserAgentUtil;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountPasswordAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountSnsCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountVerifyCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserAccountSnsCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserPasswordAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserVerifyCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserPasswordAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserVerifyCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.core.AccountAuthType;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountPasswordAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountSnsCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account.OAuthAccountVerifyCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserAccountSnsCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserPasswordAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user.OAuthAppUserVerifyCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserPasswordAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user.OAuthTenantAppUserVerifyCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.auth.modules.ip2region.Ip2RegionService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationFailureExpiredEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Component
public class AuthBadLoginLogHandler {

	private final Service service;

	public AuthBadLoginLogHandler(Service service) {
		this.service = service;
	}


	/**
	 * 认证凭证错误 事件监听
	 *
	 * @param event 事件
	 */
	@NewSpan
	@EventListener(AuthenticationFailureBadCredentialsEvent.class)
	public void authenticationFailureBadCredentialsEventListener(AuthenticationFailureBadCredentialsEvent event) {
		handler(event);
	}

	/**
	 * 认证过期 事件监听
	 *
	 * @param event 事件
	 */
	@EventListener(AuthenticationFailureExpiredEvent.class)
	@NewSpan
	public void authenticationFailureExpiredEventListener(AuthenticationFailureExpiredEvent event) {
		handler(event);
	}


	/**
	 * 用户被锁定 事件监听
	 *
	 * @param event 事件
	 */
	@EventListener(AuthenticationFailureLockedEvent.class)
	@NewSpan
	public void authenticationFailureLockedEventListener(AuthenticationFailureLockedEvent event) {
		handler(event);
	}

	/**
	 * 用户被禁用 事件监听
	 *
	 * @param event 事件
	 */
	@EventListener(AuthenticationFailureDisabledEvent.class)
	@NewSpan
	public void authenticationFailureDisabledEventListener(AuthenticationFailureDisabledEvent event) {
		handler(event);
	}


	public void handler(AbstractAuthenticationFailureEvent event) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String clientIP = JakartaServletUtil.getClientIP(request);
		String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("unknown");
		LocalDateTime loginTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault());

		String errMsg;

		if (event.getException() instanceof OAuth2AuthenticationException) {
			OAuth2Error error = ((OAuth2AuthenticationException) event.getException()).getError();
			errMsg = error.getErrorCode() + "/" + error.getDescription();
		} else {
			errMsg = event.getException().getMessage();
		}

		if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) event.getAuthentication();
			// 账号身份密码模式登录失败
			service.recordAccountAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof CairoAccountPasswordAuthenticationToken) {
			// cairo account:password 账号身份密码模式登录失败
			CairoAccountPasswordAuthenticationToken authentication = (CairoAccountPasswordAuthenticationToken) event.getAuthentication();
			service.recordAccountAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuthAccountPasswordAuthenticationToken) {
			// oauth account:password 账号身份密码模式登录失败
			OAuthAccountPasswordAuthenticationToken authentication = (OAuthAccountPasswordAuthenticationToken) event.getAuthentication();
			service.recordAccountAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof CairoAccountVerifyCodeAuthenticationToken) {
			// cairo account:verify_code 账号验证码模式登录失败
			CairoAccountVerifyCodeAuthenticationToken authentication = (CairoAccountVerifyCodeAuthenticationToken) event.getAuthentication();
			service.recordAccountAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuthAccountVerifyCodeAuthenticationToken) {
			// oauth account:verify_code 账号身份验证码模式登录失败
			OAuthAccountVerifyCodeAuthenticationToken authentication = (OAuthAccountVerifyCodeAuthenticationToken) event.getAuthentication();
			service.recordAccountAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof CairoAccountSnsCodeAuthenticationToken) {
			// cairo account:sns_code 账号第三方认证模式登录失败
			CairoAccountSnsCodeAuthenticationToken authentication = (CairoAccountSnsCodeAuthenticationToken) event.getAuthentication();
			service.recordAccountAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuthAccountSnsCodeAuthenticationToken) {
			// oauth account:sns_code 账号第三方认证模式登录失败
			OAuthAccountSnsCodeAuthenticationToken authentication = (OAuthAccountSnsCodeAuthenticationToken) event.getAuthentication();
			service.recordAccountAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof CairoAppUserPasswordAuthenticationToken) {
			// cairo app_user:account_password 终端用户-账号密码模式 登录失败
			CairoAppUserPasswordAuthenticationToken authentication = (CairoAppUserPasswordAuthenticationToken) event.getAuthentication();
			service.recordAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuthAppUserPasswordAuthenticationToken) {
			// oauth2 app_user:account_password 终端用户-账号密码模式 登录失败
			OAuthAppUserPasswordAuthenticationToken authentication = (OAuthAppUserPasswordAuthenticationToken) event.getAuthentication();
			service.recordAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof CairoAppUserVerifyCodeAuthenticationToken) {
			// cairo app_user:account_verify_code 终端用户-账号验证码模式 登录失败
			CairoAppUserVerifyCodeAuthenticationToken authentication = (CairoAppUserVerifyCodeAuthenticationToken) event.getAuthentication();
			service.recordAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuthAppUserVerifyCodeAuthenticationToken) {
			// oauth2 app_user:account_verify_code 终端用户-账号验证码模式 登录失败
			OAuthAppUserVerifyCodeAuthenticationToken authentication = (OAuthAppUserVerifyCodeAuthenticationToken) event.getAuthentication();
			service.recordAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof CairoAppUserAccountSnsCodeAuthenticationToken) {
			// auth app_user:account_sns_code 终端用户-账号第三方认证模式登录失败
			CairoAppUserAccountSnsCodeAuthenticationToken authentication = (CairoAppUserAccountSnsCodeAuthenticationToken) event.getAuthentication();
			service.recordAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuthAppUserAccountSnsCodeAuthenticationToken) {
			// oauth app_user:account_verify_code 终端用户-账号第三方认证模式登录失败
			OAuthAppUserAccountSnsCodeAuthenticationToken authentication = (OAuthAppUserAccountSnsCodeAuthenticationToken) event.getAuthentication();
			service.recordAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof CairoTenantAppUserPasswordAuthenticationToken) {
			// cairo tenant_app_user:account_password
			CairoTenantAppUserPasswordAuthenticationToken authentication = (CairoTenantAppUserPasswordAuthenticationToken) event.getAuthentication();
			service.recordTenantAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuthTenantAppUserPasswordAuthenticationToken) {
			// oauth2 tenant_app_user:account_password
			OAuthTenantAppUserPasswordAuthenticationToken authentication = (OAuthTenantAppUserPasswordAuthenticationToken) event.getAuthentication();
			service.recordTenantAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof CairoTenantAppUserVerifyCodeAuthenticationToken) {
			// cairo tenant_app_user:account_verify_code 登录失败
			CairoTenantAppUserVerifyCodeAuthenticationToken authentication = (CairoTenantAppUserVerifyCodeAuthenticationToken) event.getAuthentication();
			service.recordTenantAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuthTenantAppUserVerifyCodeAuthenticationToken) {
			// oauth2 tenant_app_user:account_verify_code 登录失败
			OAuthTenantAppUserVerifyCodeAuthenticationToken authentication = (OAuthTenantAppUserVerifyCodeAuthenticationToken) event.getAuthentication();
			service.recordTenantAppUserAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuth2ClientAuthenticationToken) {
			// oauth2 client 登录失败
			OAuth2ClientAuthenticationToken authentication = (OAuth2ClientAuthenticationToken) event.getAuthentication();
			service.recordClientAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuth2ClientCredentialsAuthenticationToken) {
			// oauth2 client模式 登录失败
			OAuth2ClientCredentialsAuthenticationToken authentication = (OAuth2ClientCredentialsAuthenticationToken) event.getAuthentication();
			service.recordClientAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else if (event.getAuthentication() instanceof OAuth2RefreshTokenAuthenticationToken) {
			// oauth2 refresh_token 登录失败
			OAuth2RefreshTokenAuthenticationToken authentication = (OAuth2RefreshTokenAuthenticationToken) event.getAuthentication();
			service.recordClientAuthFailureLog(errMsg, clientIP, userAgent, loginTime, authentication);
		} else {
			log.warn("auth {} ignore: {}", event.getClass().getSimpleName(), event.getAuthentication());
		}

	}


	@Component
	public static class Service {
		private final MongoTemplate mongoTemplate;

		private final MongoTemplate readMongoTemplate;
		private final RabbitTemplate rabbitTemplate;
		private final CairoRabbitmqTool cairoRabbitmqTool;
		private final ObjectMapper objectMapper;
		private final Ip2RegionService ip2RegionService;

		public Service(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate, MongoTemplate readMongoTemplate, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool, ObjectMapper objectMapper, Ip2RegionService ip2RegionService) {
			this.mongoTemplate = mongoTemplate;
			this.readMongoTemplate = readMongoTemplate;
			this.rabbitTemplate = rabbitTemplate;
			this.cairoRabbitmqTool = cairoRabbitmqTool;
			this.objectMapper = objectMapper;
			this.ip2RegionService = ip2RegionService;
		}

		/**
		 * 添加账号认证失败记录(基于账号密码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAccountAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, UsernamePasswordAuthenticationToken token) {
			String username = token.getName();

			Criteria accountCriteria = new Criteria();
			accountCriteria = accountCriteria.orOperator(
				Criteria.where(AccountMongodb.FIELD.USERNAME).is(username),
				Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(username),
				Criteria.where(AccountMongodb.FIELD.EMAIL).is(username)
			);
			Query userQuery = Query.query(accountCriteria);
			userQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
			AccountMongodb account = readMongoTemplate.findOne(userQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

			if (account != null && account.getAccountId() != null) {
				counterAccountFailCount(PasswordType.PASSWORD, account.getAccountId()); // 账号登录失败计数
				insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
			} else {
				insertAccountFailLog("unknown_username:" + username, LoginType.PASSWORD, null, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
			}
		}

		/**
		 * 添加账号认证失败记录(基于账号密码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAccountAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, CairoAccountPasswordAuthenticationToken token) {
			String username = token.getPrincipal().toString();

			Criteria accountCriteria = new Criteria();
			accountCriteria = accountCriteria.orOperator(
				Criteria.where(AccountMongodb.FIELD.USERNAME).is(username),
				Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(username),
				Criteria.where(AccountMongodb.FIELD.EMAIL).is(username)
			);
			Query accountQuery = Query.query(accountCriteria);
			accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

			if (account != null && account.getAccountId() != null) {
				counterAccountFailCount(PasswordType.PASSWORD, account.getAccountId()); // 账号登录失败计数
				insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
			} else {
				insertAccountFailLog("unknown_username:" + username, LoginType.PASSWORD, null, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
			}
		}

		/**
		 * 添加账号认证失败记录(基于账号手机号验证码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAccountAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, CairoAccountVerifyCodeAuthenticationToken token) {
			String phoneNumber = token.getPhoneNumber();

			Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(phoneNumber);
			Query accountQuery = Query.query(accountCriteria);
			accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

			if (account != null && account.getAccountId() != null) {
				counterAccountFailCount(PasswordType.VERIFY_CODE, account.getAccountId()); // 账号登录失败计数
				insertAccountFailLog(account.getAccountId(), LoginType.VERIFY_CODE, null, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
			} else {
				insertAccountFailLog("unknown_phone_number:" + phoneNumber, LoginType.VERIFY_CODE, null, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
			}
		}

		/**
		 * 添加账号认证失败记录(基于第三方认证登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAccountAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, CairoAccountSnsCodeAuthenticationToken token) {
			String appId = token.getAppId();
			String clientId = token.getClientId();
			String snsType = token.getSnsType();
			String snsProviderId = token.getSnsProviderId();
			String snsCode = token.getSnsCode();

			insertAccountFailLog("unknown_sns:" + String.format("%s_%s", snsProviderId, snsCode), LoginType.SNS, snsType, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
		}

		/**
		 * 记录账号认证失败日志(基于OAuth账号密码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAccountAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuthAccountPasswordAuthenticationToken token) {
			String username = token.getUsername();
			OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) token.getPrincipal();
			if (clientPrincipal.getRegisteredClient() instanceof CairoRegisteredClient) {
				String clientId = clientPrincipal.getRegisteredClient().getClientId();
				String appId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getAppId();

				insertClientFailLog(token.getGrantType(), null, appId, clientId, loginTime, errMsg, ip, userAgent);

				Criteria accountCriteria = new Criteria();
				accountCriteria = accountCriteria.orOperator(
					Criteria.where(AccountMongodb.FIELD.USERNAME).is(username),
					Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(username),
					Criteria.where(AccountMongodb.FIELD.EMAIL).is(username)
				);
				Query accountQuery = Query.query(accountCriteria);
				accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
				AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

				if (account != null && account.getAccountId() != null) {
					counterAccountFailCount(PasswordType.PASSWORD, account.getAccountId()); // 账号登录失败计数
					insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
				} else {
					insertAccountFailLog("unknown_username:" + username, LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
				}

			} else {
				log.warn("add account failure log unknown principal: {}", token.getPrincipal());
			}
		}


		/**
		 * 记录账号认证失败日志(基于OAuth账号验证码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAccountAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuthAccountVerifyCodeAuthenticationToken token) {
			String phoneNumber = token.getPhoneNumber();
			OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) token.getPrincipal();
			if (clientPrincipal.getRegisteredClient() instanceof CairoRegisteredClient) {
				String clientId = clientPrincipal.getRegisteredClient().getClientId();
				String appId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getAppId();

				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(phoneNumber);
				Query accountQuery = Query.query(accountCriteria);
				accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
				AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

				if (account != null && account.getAccountId() != null) {
					counterAccountFailCount(PasswordType.VERIFY_CODE, account.getAccountId()); // 账号登录失败计数
					insertAccountFailLog(account.getAccountId(), LoginType.VERIFY_CODE, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志
				} else {
					insertAccountFailLog("unknown_phone_number:" + phoneNumber, LoginType.VERIFY_CODE, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志
				}
			} else {
				log.warn("add account failure log unknown principal: {}", token.getPrincipal());
			}
		}

		/**
		 * 记录账号认证失败日志(基于OAuth账号验证码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAccountAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuthAccountSnsCodeAuthenticationToken token) {
			String snsType = token.getSnsType();
			String snsProviderId = token.getSnsProviderId();
			String snsCode = token.getSnsCode();

			OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) token.getPrincipal();
			if (clientPrincipal.getRegisteredClient() instanceof CairoRegisteredClient) {
				String clientId = clientPrincipal.getRegisteredClient().getClientId();
				String appId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getAppId();
				insertClientFailLog(token.getGrantType(), null, appId, clientId, loginTime, errMsg, ip, userAgent);
				insertAccountFailLog("unknown_account_sns:" + String.format("%s:%s", snsProviderId, snsCode), LoginType.SNS, snsType, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志
			} else {
				log.warn("add account failure log unknown principal: {}", token.getPrincipal());
			}
		}

		/**
		 * 记录用户认证失败日志(基于终端用户账号密码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, CairoAppUserPasswordAuthenticationToken token) {
			String appId = token.getAppId();
			String endpointId = token.getEndpointId();
			String clientId = token.getClientId();
			String username = token.getUsername();

			Criteria accountCriteria = new Criteria()
				.orOperator(
					Criteria.where(AccountMongodb.FIELD.USERNAME).is(username),
					Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(username),
					Criteria.where(AccountMongodb.FIELD.EMAIL).is(username)
				);
			Query accountQuery = Query.query(accountCriteria);
			accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

			if (account != null && account.getAccountId() != null) {
				counterAccountFailCount(PasswordType.VERIFY_CODE, account.getAccountId()); // 账号登录失败计数
				insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志

				Criteria userCriteria = Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
				Query userQuery = Query.query(userCriteria);
				userQuery.fields().include(AppUserMongodb.FIELD.USER_ID);
				AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

				// 终端用户登录日志
				if (user != null && user.getUserId() != null) {
					insertAppUserFailLog(LoginType.PASSWORD, null, appId, endpointId, clientId, user.getUserId(), loginTime, errMsg, ip, userAgent); // 添加OAuth终端用户登录失败日志
				} else {
					insertAppUserFailLog(LoginType.PASSWORD, null, appId, endpointId, clientId, "unknown_username:" + username, loginTime, errMsg, ip, userAgent); // 添加OAuth终端用户登录失败日志
				}
			} else {
				insertAccountFailLog("unknown_username:" + username, LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志
			}
		}

		/**
		 * 记录用户认证失败日志(基于OAuth终端用户账号密码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuthAppUserPasswordAuthenticationToken token) {
			String username = token.getUsername();
			OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) token.getPrincipal();
			if (clientPrincipal.getRegisteredClient() instanceof CairoRegisteredClient) {
				String appId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getAppId();
				String endpointId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getEndpointId();
				String clientId = clientPrincipal.getRegisteredClient().getClientId();
				insertClientFailLog(token.getGrantType(), null, appId, clientId, loginTime, errMsg, ip, userAgent);

				Criteria accountCriteria = new Criteria()
					.orOperator(
						Criteria.where(AccountMongodb.FIELD.USERNAME).is(username),
						Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(username),
						Criteria.where(AccountMongodb.FIELD.EMAIL).is(username)
					);
				Query accountQuery = Query.query(accountCriteria);
				accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
				AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

				if (account != null && account.getAccountId() != null) {
					counterAccountFailCount(PasswordType.PASSWORD, account.getAccountId()); // 账号登录失败计数
					insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志

					Criteria userCriteria = Criteria
						.where(AppUserMongodb.FIELD.APP_ID).is(appId)
						.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
					Query userQuery = Query.query(userCriteria);
					userQuery.fields().include(AppUserMongodb.FIELD.USER_ID);
					AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);


					// 终端用户登录日志
					if (user != null && user.getUserId() != null) {
						insertAppUserFailLog(LoginType.PASSWORD, null, appId, endpointId, clientId, user.getUserId(), loginTime, errMsg, ip, userAgent); // 添加OAuth终端用户登录失败日志
					} else {
						insertAppUserFailLog(LoginType.PASSWORD, null, appId, endpointId, clientId, "unknown_username:" + username, loginTime, errMsg, ip, userAgent); // 添加OAuth终端用户登录失败日志
					}

				} else {
					insertAccountFailLog("unknown_username:" + username, LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志
				}
			} else {
				log.warn("add app endpoint user failure log unknown principal: {}", token.getPrincipal());
			}
		}

		/**
		 * 记录用户认证失败日志(基于终端用户账号手机号验证码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, CairoAppUserVerifyCodeAuthenticationToken token) {
			String appId = token.getAppId();
			String endpointId = token.getEndpointId();
			String clientId = token.getClientId();
			String phoneNumber = token.getPhoneNumber();

			Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(phoneNumber);
			Query accountQuery = Query.query(accountCriteria);
			accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

			if (account != null && account.getAccountId() != null) {
				counterAccountFailCount(PasswordType.VERIFY_CODE, account.getAccountId()); // 账号登录失败计数
				insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志

				Criteria userCriteria = Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
				Query userQuery = Query.query(userCriteria);
				userQuery.fields().include(AppUserMongodb.FIELD.USER_ID);
				AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);


				// 终端用户登录日志
				if (user != null && user.getUserId() != null) {
					insertAppUserFailLog(LoginType.VERIFY_CODE, null, appId, endpointId, clientId, user.getUserId(), loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
				} else {
					insertAppUserFailLog(LoginType.VERIFY_CODE, null, appId, endpointId, clientId, "unknown_phone_number:" + phoneNumber, loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
				}
			} else {
				insertAccountFailLog("unknown_phone_number:" + phoneNumber, LoginType.VERIFY_CODE, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
			}
		}

		/**
		 * 记录用户认证失败日志(基于终端用户账号手机号验证码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, CairoAppUserAccountSnsCodeAuthenticationToken token) {
			String appId = token.getAppId();
			String endpointId = token.getEndpointId();
			String clientId = token.getClientId();
			String snsType = token.getSnsType();
			String snsProviderId = token.getSnsProviderId();
			String snsCode = token.getSnsCode();
			String accountId = "unknown_sns:" + String.format("%s_%s", snsProviderId, snsCode);
			String userId = "unknown_sns:" + String.format("%s_%s", snsProviderId, snsCode);

			insertAccountFailLog(accountId, LoginType.SNS, snsType, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
			insertAppUserFailLog(LoginType.SNS, snsType, appId, endpointId, clientId, userId, loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
		}


		/**
		 * 记录用户认证失败日志(基于OAuth终端用户账号手机号验证码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuthAppUserVerifyCodeAuthenticationToken token) {
			String phoneNumber = token.getPhoneNumber();
			OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) token.getPrincipal();
			if (clientPrincipal.getRegisteredClient() instanceof CairoRegisteredClient) {
				String appId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getAppId();
				String endpointId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getEndpointId();
				String clientId = clientPrincipal.getRegisteredClient().getClientId();
				insertClientFailLog(token.getGrantType(), null, appId, clientId, loginTime, errMsg, ip, userAgent);

				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(phoneNumber);
				Query accountQuery = Query.query(accountCriteria);
				accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
				AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

				if (account != null && account.getAccountId() != null) {
					counterAccountFailCount(PasswordType.VERIFY_CODE, account.getAccountId()); // 账号登录失败计数
					insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志

					Criteria userCriteria = Criteria
						.where(AppUserMongodb.FIELD.APP_ID).is(appId)
						.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
					Query userQuery = Query.query(userCriteria);
					userQuery.fields().include(AppUserMongodb.FIELD.USER_ID);
					AppUserMongodb appUser = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);


					// 终端用户登录日志
					if (appUser != null && appUser.getUserId() != null) {
						insertAppUserFailLog(LoginType.VERIFY_CODE, null, appId, endpointId, clientId, appUser.getUserId(), loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
					} else {
						insertAppUserFailLog(LoginType.VERIFY_CODE, null, appId, endpointId, clientId, "unknown_phone_number:" + phoneNumber, loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
					}
				} else {
					insertAccountFailLog("unknown_phone_number:" + phoneNumber, LoginType.VERIFY_CODE, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
				}
			} else {
				log.warn("add app endpoint user failure log unknown principal: {}", token.getPrincipal());
			}
		}

		/**
		 * 记录用户认证失败日志(基于OAuth终端用户账号手机号验证码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuthAppUserAccountSnsCodeAuthenticationToken token) {
			String snsType = token.getSnsType();
			String snsProviderId = token.getSnsProviderId();
			String snsCode = token.getSnsCode();
			OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) token.getPrincipal();
			if (clientPrincipal.getRegisteredClient() instanceof CairoRegisteredClient) {
				String appId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getAppId();
				String endpointId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getEndpointId();
				String clientId = clientPrincipal.getRegisteredClient().getClientId();
				String accountId = "unknown_sns:" + String.format("%s_%s", snsProviderId, snsCode);
				String userId = "unknown_sns:" + String.format("%s_%s", snsProviderId, snsCode);
				insertClientFailLog(token.getGrantType(), null, appId, clientId, loginTime, errMsg, ip, userAgent);
				insertAccountFailLog(accountId, LoginType.SNS, snsType, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
				insertAppUserFailLog(LoginType.SNS, snsType, appId, endpointId, clientId, userId, loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
			} else {
				log.warn("add app endpoint user failure log unknown principal: {}", token.getPrincipal());
			}
		}


		/**
		 * 记录用户认证失败日志(基于终端用户账号密码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordTenantAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, CairoTenantAppUserPasswordAuthenticationToken token) {
			String tenantId = token.getTenantId();
			String appId = token.getAppId();
			String endpointId = token.getEndpointId();
			String clientId = token.getClientId();
			String username = token.getUsername();

			Criteria accountCriteria = new Criteria()
				.orOperator(
					Criteria.where(AccountMongodb.FIELD.USERNAME).is(username),
					Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(username),
					Criteria.where(AccountMongodb.FIELD.EMAIL).is(username)
				);
			Query accountQuery = Query.query(accountCriteria);
			accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

			if (account != null && account.getAccountId() != null) {
				counterAccountFailCount(PasswordType.VERIFY_CODE, account.getAccountId()); // 账号登录失败计数
				insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志

				Criteria userCriteria = Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
				Query userQuery = Query.query(userCriteria);
				userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID);
				TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

				// 终端用户登录日志
				if (user != null && user.getUserId() != null) {
					insertTenantAppUserFailLog(LoginType.PASSWORD, null, tenantId, appId, endpointId, clientId, user.getUserId(), loginTime, errMsg, ip, userAgent); // 添加OAuth终端用户登录失败日志
				} else {
					insertTenantAppUserFailLog(LoginType.PASSWORD, null, tenantId, appId, endpointId, clientId, "unknown_username:" + username, loginTime, errMsg, ip, userAgent); // 添加OAuth终端用户登录失败日志
				}
			} else {
				insertAccountFailLog("unknown_username:" + username, LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志
			}
		}

		/**
		 * 记录用户认证失败日志(基于OAuth终端用户账号密码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordTenantAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuthTenantAppUserPasswordAuthenticationToken token) {
			String tenantId = token.getTenantId();
			String username = token.getUsername();
			OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) token.getPrincipal();
			if (clientPrincipal.getRegisteredClient() instanceof CairoRegisteredClient) {
				String appId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getAppId();
				String endpointId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getEndpointId();
				String clientId = clientPrincipal.getRegisteredClient().getClientId();
				insertClientFailLog(token.getGrantType(), null, appId, clientId, loginTime, errMsg, ip, userAgent);

				Criteria accountCriteria = new Criteria()
					.orOperator(
						Criteria.where(AccountMongodb.FIELD.USERNAME).is(username),
						Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(username),
						Criteria.where(AccountMongodb.FIELD.EMAIL).is(username)
					);
				Query accountQuery = Query.query(accountCriteria);
				accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
				AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

				if (account != null && account.getAccountId() != null) {
					counterAccountFailCount(PasswordType.PASSWORD, account.getAccountId()); // 账号登录失败计数
					insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志

					Criteria userCriteria = Criteria
						.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
						.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
					Query userQuery = Query.query(userCriteria);
					userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID);
					TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);


					// 终端用户登录日志
					if (user != null && user.getUserId() != null) {
						insertTenantAppUserFailLog(LoginType.PASSWORD, null, tenantId, appId, endpointId, clientId, user.getUserId(), loginTime, errMsg, ip, userAgent); // 添加OAuth终端用户登录失败日志
					} else {
						insertTenantAppUserFailLog(LoginType.PASSWORD, null, tenantId, appId, endpointId, clientId, "unknown_username:" + username, loginTime, errMsg, ip, userAgent); // 添加OAuth终端用户登录失败日志
					}

				} else {
					insertAccountFailLog("unknown_username:" + username, LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加OAuth账号登录失败日志
				}
			} else {
				log.warn("add endpoint user failure log unknown principal: {}", token.getPrincipal());
			}
		}

		/**
		 * 记录用户认证失败日志(基于终端用户账号手机号验证码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordTenantAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, CairoTenantAppUserVerifyCodeAuthenticationToken token) {
			String tenantId = token.getTenantId();
			String appId = token.getAppId();
			String endpointId = token.getEndpointId();
			String clientId = token.getClientId();
			String phoneNumber = token.getPhoneNumber();

			Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(phoneNumber);
			Query accountQuery = Query.query(accountCriteria);
			accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

			if (account != null && account.getAccountId() != null) {
				counterAccountFailCount(PasswordType.VERIFY_CODE, account.getAccountId()); // 账号登录失败计数
				insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志

				Criteria userCriteria = Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
				Query userQuery = Query.query(userCriteria);
				userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID);
				TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);


				// 终端用户登录日志
				if (user != null && user.getUserId() != null) {
					insertTenantAppUserFailLog(LoginType.VERIFY_CODE, null, tenantId, appId, endpointId, clientId, user.getUserId(), loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
				} else {
					insertTenantAppUserFailLog(LoginType.VERIFY_CODE, null, tenantId, appId, endpointId, clientId, "unknown_phone_number:" + phoneNumber, loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
				}
			} else {
				insertAccountFailLog("unknown_phone_number:" + phoneNumber, LoginType.VERIFY_CODE, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
			}
		}


		/**
		 * 记录用户认证失败日志(基于OAuth终端用户账号手机号验证码登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordTenantAppUserAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuthTenantAppUserVerifyCodeAuthenticationToken token) {
			String tenantId = token.getTenantId();
			String phoneNumber = token.getPhoneNumber();
			OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) token.getPrincipal();
			if (clientPrincipal.getRegisteredClient() instanceof CairoRegisteredClient) {
				String appId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getAppId();
				String endpointId = ((CairoRegisteredClient) clientPrincipal.getRegisteredClient()).getEndpointId();
				String clientId = clientPrincipal.getRegisteredClient().getClientId();
				insertClientFailLog(token.getGrantType(), null, appId, clientId, loginTime, errMsg, ip, userAgent);

				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(phoneNumber);
				Query accountQuery = Query.query(accountCriteria);
				accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID);
				AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

				if (account != null && account.getAccountId() != null) {
					counterAccountFailCount(PasswordType.VERIFY_CODE, account.getAccountId()); // 账号登录失败计数
					insertAccountFailLog(account.getAccountId(), LoginType.PASSWORD, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志

					Criteria userCriteria = Criteria
						.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
						.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
					Query userQuery = Query.query(userCriteria);
					userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID);
					TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);


					// 终端用户登录日志
					if (user != null && user.getUserId() != null) {
						insertTenantAppUserFailLog(LoginType.VERIFY_CODE, null, tenantId, appId, endpointId, clientId, user.getUserId(), loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
					} else {
						insertTenantAppUserFailLog(LoginType.VERIFY_CODE, null, tenantId, appId, endpointId, clientId, "unknown_phone_number:" + phoneNumber, loginTime, errMsg, ip, userAgent); // 添加终端用户登录失败日志
					}
				} else {
					insertAccountFailLog("unknown_phone_number:" + phoneNumber, LoginType.VERIFY_CODE, null, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加账号登录失败日志
				}
			} else {
				log.warn("add endpoint user failure log unknown principal: {}", token.getPrincipal());
			}
		}

		/**
		 * 记录客户端认证失败日志(基于OAuth客户端密钥登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordClientAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuth2ClientCredentialsAuthenticationToken token) {
			if (token.getPrincipal() instanceof OAuth2ClientAuthenticationToken) {
				recordClientAuthFailureLog(errMsg, ip, userAgent, loginTime, (OAuth2ClientAuthenticationToken) token.getPrincipal());
			} else {
				log.warn("add client failure log unknown principal: {}", token.getPrincipal());
			}
		}

		/**
		 * 记录客户端认证失败日志(基于OAuth客户端访问令牌登录)
		 *
		 * @param errMsg    错误日志
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordClientAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuth2ClientAuthenticationToken token) {
			String clientId = token.getPrincipal().toString();
			ClientAuthenticationMethod clientMethod = token.getClientAuthenticationMethod();
			if (token.getRegisteredClient() != null && token.getRegisteredClient() instanceof CairoRegisteredClient) {
				String appId = ((CairoRegisteredClient) token.getRegisteredClient()).getAppId();
				insertClientFailLog(AuthorizationGrantType.CLIENT_CREDENTIALS, clientMethod, appId, clientId, loginTime, errMsg, ip, userAgent); // 添加客户端登录失败日志
			} else {
				insertClientFailLog(AuthorizationGrantType.CLIENT_CREDENTIALS, clientMethod, "unknown", clientId, loginTime, errMsg, ip, userAgent); // 添加客户端登录失败日志
			}
		}


		/**
		 * 记录客户端刷新令牌认证失败日志(基于OAuth刷新令牌认证)
		 *
		 * @param errMsg    错误消息
		 * @param ip        ip
		 * @param userAgent userAgent
		 * @param loginTime loginTime
		 * @param token     token
		 */
		@Async
		void recordClientAuthFailureLog(String errMsg, String ip, String userAgent, LocalDateTime loginTime, OAuth2RefreshTokenAuthenticationToken token) {
			if (token.getPrincipal() instanceof OAuth2ClientAuthenticationToken) {
				recordClientAuthFailureLog(errMsg, ip, userAgent, loginTime, (OAuth2ClientAuthenticationToken) token.getPrincipal()); // 添加客户端登录失败日志
			} else {
				log.warn("add refresh token client failure log unknown principal: {}", token.getPrincipal());
			}
		}


		/**
		 * 客户端登录日志添加到数据库层
		 *
		 * @param clientAuthenticationMethod 鉴权方式
		 * @param appId                      appId
		 * @param clientId                   clientId
		 * @param loginTime                  登录时间
		 * @param errMsg                     错误消息
		 * @param ip                         ip
		 * @param userAgent                  userAgent
		 */
		private void insertClientFailLog(AuthorizationGrantType grantType, ClientAuthenticationMethod clientAuthenticationMethod, String appId, String clientId, LocalDateTime loginTime, String errMsg, String ip, String userAgent) {
			UserAgent agent = CairoUserAgentUtil.parse(userAgent);

			String region = ip2RegionService.getRegionStr(ip);

			ClientLoginLogMongodb clientLoginLogMongodb = ClientLoginLogMongodb.builder()
				.logId(CoreConstants.SNOWFLAKE.nextIdStr())
				.appId(appId)
				.clientId(clientId)
				.loginTime(loginTime)
				.success(false)
				.errMsg(errMsg)
				.grantType(Optional.ofNullable(grantType).map(AuthorizationGrantType::getValue).orElse(null))
				.method(Optional.ofNullable(clientAuthenticationMethod).map(ClientAuthenticationMethod::getValue).orElse(null))
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

		/**
		 * 账号登录日志添加到数据库层
		 *
		 * @param loginType 登录方式
		 * @param accountId 账号ID
		 * @param snsType   第三方认证登录
		 * @param loginTime 登录时间
		 * @param errMsg    错误消息
		 * @param ip        ip
		 * @param userAgent userAgent
		 */
		private void insertAccountFailLog(String accountId, LoginType loginType, String snsType, LocalDateTime loginTime, String errMsg, String ip, String userAgent) {
			UserAgent agent = CairoUserAgentUtil.parse(userAgent);

			String region = ip2RegionService.getRegionStr(ip);

			AccountLoginLogMongodb accountLoginLogMongodb = AccountLoginLogMongodb.builder()
				.logId(CoreConstants.SNOWFLAKE.nextIdStr())
				.loginTime(loginTime)
				.accountId(accountId)
				.accountTokenId(accountId)
				.authType(AccountAuthType.SSO.getValue())
				.loginType(loginType.getValue())
				.snsType(snsType)
				.appId(null)
				.clientId(null)
				.success(false)
				.errMsg(errMsg)
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

		/**
		 * 账号登录日志添加到数据库层
		 *
		 * @param accountId 账号ID
		 * @param loginType 登录方式
		 * @param appId     应用ID
		 * @param clientId  客户端ID
		 * @param loginTime 登录时间
		 * @param errMsg    错误消息
		 * @param ip        ip
		 * @param userAgent userAgent
		 */
		private void insertAccountFailLog(String accountId, LoginType loginType, String snsType, String appId, String clientId, LocalDateTime loginTime, String errMsg, String ip, String userAgent) {
			UserAgent agent = CairoUserAgentUtil.parse(userAgent);
			String region = ip2RegionService.getRegionStr(ip);

			AccountLoginLogMongodb accountLoginLogMongodb = AccountLoginLogMongodb.builder()
				.logId(CoreConstants.SNOWFLAKE.nextIdStr())
				.loginTime(loginTime)
				.accountId(accountId)
				.accountTokenId(null)
				.authType(AccountAuthType.OAUTH2.getValue())
				.loginType(loginType.getValue())
				.snsType(snsType)
				.appId(appId)
				.clientId(clientId)
				.success(false)
				.errMsg(errMsg)
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

		/**
		 * 终端用户登录日志添加到数据库层
		 *
		 * @param loginType     登录方式
		 * @param snsType       第三方认证方式
		 * @param appId         应用ID
		 * @param endpointId 终端ID
		 * @param clientId      客户端ID
		 * @param userId        用户ID
		 * @param loginTime     登录时间
		 * @param errMsg        错误消息
		 * @param ip            ip
		 * @param userAgent     userAgent
		 */
		private void insertAppUserFailLog(LoginType loginType, String snsType, String appId, String endpointId, String clientId, String userId, LocalDateTime loginTime, String errMsg, String ip, String userAgent) {
			UserAgent agent = CairoUserAgentUtil.parse(userAgent);
			String region = ip2RegionService.getRegionStr(ip);

			AppUserLoginLogMongodb appUserLoginLogMongodb = AppUserLoginLogMongodb.builder()
				.logId(CoreConstants.SNOWFLAKE.nextIdStr())
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.userId(userId)
				.loginTime(loginTime)
				.loginType(loginType.getValue())
				.snsType(snsType)
				.success(false)
				.errMsg(errMsg)
				.ip(ip)
				.region(region)
				.agent(userAgent)
				.os(Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.platform(Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null))
				.engine(Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.app(Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.mobile(agent.isMobile())
				.metadata(AppUserMetadataMongodb.builder()
					.build())
				.build();
			mongoTemplate.insert(appUserLoginLogMongodb, MongodbConstants.Collection.APP_USER_LOGIN_LOG);
		}

		/**
		 * 终端用户登录日志添加到数据库层
		 *
		 * @param loginType     登录方式
		 * @param snsType       第三方认证方式
		 * @param tenantId      租户ID
		 * @param appId         应用ID
		 * @param endpointId 终端ID
		 * @param clientId      客户端ID
		 * @param userId        用户ID
		 * @param loginTime     登录时间
		 * @param errMsg        错误消息
		 * @param ip            ip
		 * @param userAgent     userAgent
		 */
		private void insertTenantAppUserFailLog(LoginType loginType, String snsType, String tenantId, String appId, String endpointId, String clientId, String userId, LocalDateTime loginTime, String errMsg, String ip, String userAgent) {
			UserAgent agent = CairoUserAgentUtil.parse(userAgent);
			String region = ip2RegionService.getRegionStr(ip);

			TenantAppUserLoginLogMongodb tenantAppUserLoginLogMongodb = TenantAppUserLoginLogMongodb.builder()
				.logId(CoreConstants.SNOWFLAKE.nextIdStr())
				.tenantId(tenantId)
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.userId(userId)
				.loginTime(loginTime)
				.loginType(loginType.getValue())
				.snsType(snsType)
				.success(false)
				.errMsg(errMsg)
				.ip(ip)
				.region(region)
				.agent(userAgent)
				.os(Optional.ofNullable(agent.getOs()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.platform(Optional.ofNullable(agent.getPlatform()).map(UserAgentInfo::getName).orElse(null))
				.engine(Optional.ofNullable(agent.getEngine()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.app(Optional.ofNullable(agent.getBrowser()).map(x -> x.getName() + "/" + x.getVersion(userAgent)).orElse(null))
				.mobile(agent.isMobile())
				.metadata(AppUserMetadataMongodb.builder()
					.build())
				.build();
			mongoTemplate.insert(tenantAppUserLoginLogMongodb, MongodbConstants.Collection.TENANT_APP_USER_LOGIN_LOG);
		}

		/**
		 * 记录账号登录失败次数
		 *
		 * @param accountId 账号ID
		 */
		@SneakyThrows
		private void counterAccountFailCount(PasswordType passwordType, String accountId) {
			Criteria criteria = Criteria
				.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(accountId)
				.and(AccountPasswordMongodb.FIELD.TYPE).is(passwordType.getType());
			Query accountPasswordQuery = Query.query(criteria);

			AccountPasswordMongodb accountPasswordMongodb = mongoTemplate.findOne(accountPasswordQuery, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);
			if (accountPasswordMongodb == null) {
				// 没有密码情况，插入数据
				AccountPasswordMongodb newInsertedAccountPasswordMongodb = AccountPasswordMongodb.builder()
					.accountId(accountId)
					.type(passwordType.getType())
					.password(null)
					.passwordFailCount(1)
					.passwordFailTime(LocalDateTime.now())
					.metadata(AccountMetadataMongodb.builder().build())
					.build();
				mongoTemplate.insert(newInsertedAccountPasswordMongodb, MongodbConstants.Collection.ACCOUNT_PASSWORD);
			} else {
				// 有密码情况更新错误次数
				boolean lock = false;
				// 如果设置密码，错误次数累计4次(加上本次一共5次)，锁定账号10分钟
				// if (accountPasswordMongodb.getPasswordFailCount() >= 4) {
				// 	lock = true;
				// 	Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId);
				// 	Query accountQuery = Query.query(accountCriteria);
				// 	Update accountUpdate = new Update();
				// 	accountUpdate.set(AccountMongodb.FIELD.LOCKED, true);
				// 	accountUpdate.currentDate(AccountMongodb.FIELD.LOCKED_TIME);
				// 	accountUpdate.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, null);
				// 	accountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
				// 	accountUpdate.inc(AccountMongodb.FIELD.VERSION);
				// 	mongoTemplate.updateFirst(accountQuery, accountUpdate, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				//
				// 	// Duration lockDuration = Duration.ofSeconds(10);
				// 	Duration lockDuration = Duration.ofMinutes(10);
				// 	// 发送延迟账号解锁消息
				// 	rabbitTemplate.convertAndSend(
				// 		cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				// 		cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.UNLOCK_ACCOUNT),
				// 		objectMapper.writeValueAsString(UnlockAccountMessage.builder()
				// 			.accountId(accountId)
				// 			.lockedTime(LocalDateTime.now())
				// 			.eventAccountId(null)
				// 			.eventTime(LocalDateTime.now())
				// 			.build()),
				// 		message -> {
				// 			message.getMessageProperties().setDelay((int) lockDuration.toMillis());
				// 			return message;
				// 		},
				// 		new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
				// 	);
				// }

				Update accountPasswordUpdate = new Update();
				if (lock) {
					// 锁定重置失败次数
					accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_COUNT, 0);
					accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_TIME, null);
				} else {
					// 未锁定增加失败次数
					accountPasswordUpdate.inc(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_COUNT);
					accountPasswordUpdate.currentDate(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_TIME);
				}
				accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, null);
				accountPasswordUpdate.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);

				mongoTemplate.updateFirst(accountPasswordQuery, accountPasswordUpdate, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);
			}
		}
	}
}
