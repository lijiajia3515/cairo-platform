package io.github.lijiajia3515.cairo.auth.framework.security.event;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication.SimpleAccountAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication.SimpleTenantAppUserAuthenticationConverter;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.stereotype.Component;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.ACCOUNT;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.TENANT_APP_USER;

/**
 * 取消注销中账号 事件处理器
 */
@Slf4j
//@Component
public class UnLogoffAccountHandler {
	private final Service service;

	public UnLogoffAccountHandler(Service service) {
		this.service = service;
	}

	/**
	 * 取消注销账号处理器
	 *
	 * @param event 事件
	 */
	@EventListener(AuthenticationSuccessEvent.class)
	public void unLogoffAccountHandler(AuthenticationSuccessEvent event) {
		service.unLogoffAccount(event.getAuthentication());
	}

	@Component
	public static class Service {
		private final JwtDecoder jwtDecoder;
		private final MongoTemplate mongoTemplate;
		private final TenantAppUserCommonService tenantAppUserCommonService;
		private final SimpleAccountAuthenticationConverter simpleAccountAuthenticationConverter = new SimpleAccountAuthenticationConverter();
		private final SimpleTenantAppUserAuthenticationConverter simpleTenantAppUserAuthenticationConverter = new SimpleTenantAppUserAuthenticationConverter();

		public Service(JwtDecoder jwtDecoder, MongoTemplate mongoTemplate, TenantAppUserCommonService tenantAppUserCommonService) {
			this.jwtDecoder = jwtDecoder;
			this.mongoTemplate = mongoTemplate;
			this.tenantAppUserCommonService = tenantAppUserCommonService;
		}

		@Async
		public void unLogoffAccount(Authentication authentication) {
			String accountId = null;
			if (authentication instanceof OAuth2AccessTokenAuthenticationToken) {
				OAuth2AccessTokenAuthenticationToken token = (OAuth2AccessTokenAuthenticationToken) authentication;
				String authType = (String) token.getAdditionalParameters().get(CairoOAuthParameterNames.AUTH_TYPE);
				String tokenValue = token.getAccessToken().getTokenValue();
				Jwt jwtToken = jwtDecoder.decode(tokenValue);
				try {
					if (ACCOUNT.getValue().equals(authType)) {
						// 账号认证
						CairoOAuthAccountAuthenticationToken accountAuthenticationToken = simpleAccountAuthenticationConverter.convert(jwtToken);
						if (accountAuthenticationToken != null) {
							CairoOAuthAccountPrincipal accountPrincipal = accountAuthenticationToken.getPrincipal();
							accountId = accountPrincipal.getAccountId();
						}
					} else if (TENANT_APP_USER.getValue().equals(authType)) {
						// 终端用户认证
						CairoOAuthTenantAppUserAuthenticationToken endpointUserAuthenticationToken = simpleTenantAppUserAuthenticationConverter.convert(jwtToken);
						if (endpointUserAuthenticationToken != null) {
							CairoOAuthTenantAppUserPrincipal endpointUserPrincipal = endpointUserAuthenticationToken.getPrincipal();
							accountId = tenantAppUserCommonService.getAccountIdByUserId(endpointUserPrincipal.getTenantId(), endpointUserPrincipal.getAppId(), endpointUserPrincipal.getUserId())
								.orElse(null);
						}
					}
				} catch (RuntimeException e) {
					log.info("updateUnLogoffAccount jwt decode error", e);
				}
			} else if (authentication instanceof UsernamePasswordAuthenticationToken) {
				UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
				if (token.getPrincipal() instanceof CairoAuthAccount) {
					accountId = ((CairoAuthAccount) token.getPrincipal()).getAccountId();
				}
			}

			try {
				if (accountId != null) {
					Criteria criteria = Criteria
						.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId)
						.and(AccountMongodb.FIELD.LOGOFF_STATUS).is(AccountLogoffStatus.PENDING.getLogoffStatusValue());
					Query query = Query.query(criteria);
					Update update = Update.update(AccountMongodb.FIELD.LOGOFF_STATUS, AccountLogoffStatus.NO.getLogoffStatusValue());
					update.set(AccountMongodb.FIELD.LOGOFF_PENDING_TIME, null);
					update.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
					update.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, accountId);
					mongoTemplate.updateFirst(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				}
			} catch (Exception e) {
				log.info("unLogoffAccount update db error", e);
			}
		}
	}

}
