package io.github.lijiajia3515.cairo.auth.modules.biz_log;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * 业务日志切面zhgd
 */
@Slf4j
@Aspect
public class BizLogAspect {

	private final ObjectMapper objectMapper;
	private final BizLogService bizLogService;

	public BizLogAspect(ObjectMapper objectMapper, BizLogService bizLogService) {
		this.objectMapper = objectMapper;
		this.bizLogService = bizLogService;
	}

	@Around("@annotation(bizLog)")
	public Object aroundLog(ProceedingJoinPoint point, BizLog bizLog) throws Throwable {
		Object result = null;
		String errorMessage = null;
		LocalDateTime startTime = LocalDateTime.now();
		try {
			result = point.proceed();
		} catch (Exception e) {
			errorMessage = e.getMessage();
			throw e;
		} finally {
			try {
				LocalDateTime endTime = LocalDateTime.now();
				// 耗时毫秒数
				long millis = Duration.between(startTime, endTime).toMillis();
				HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
				String ip = JakartaServletUtil.getClientIP(request);
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

				String params = getParams(point, result, bizLog);
				if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
					OpenBizLog openBizLog = openBizLog(bizLog, params, errorMessage, ip, startTime, endTime, millis);
					bizLogService.storeOpenBizLog(openBizLog);
				} else if (authentication instanceof CairoOAuthClientAuthenticationToken) {
					ClientBizLog clientBizLog = clientBizLog((CairoOAuthClientAuthenticationToken) authentication, bizLog, params, errorMessage, ip, startTime, endTime, millis);
					bizLogService.storeClientBizLog(clientBizLog);
				} else if (authentication instanceof CairoOAuthAccountAuthenticationToken) {
					AccountBizLog accountBizLog = accountBizLog((CairoOAuthAccountAuthenticationToken) authentication, bizLog, params, errorMessage, ip, startTime, endTime, millis);
					bizLogService.storeAccountBizLog(accountBizLog);
				} else if (authentication instanceof CairoOAuthAppUserAuthenticationToken) {
					AppBizLog endpointBizLog = endpointBizLog((CairoOAuthAppUserAuthenticationToken) authentication, bizLog, params, errorMessage, ip, startTime, endTime, millis);
					bizLogService.storeAppBizLog(endpointBizLog);
				} else if (authentication instanceof CairoOAuthSubappUserAuthenticationToken) {
					SubappBizLog subappBizLog = subappBizLog((CairoOAuthSubappUserAuthenticationToken) authentication, bizLog, params, errorMessage, ip, startTime, endTime, millis);
					bizLogService.storeSubappBizLog(subappBizLog);
				} else if (authentication instanceof CairoOAuthTenantAppUserAuthenticationToken) {
					TenantAppBizLog tenantAppBizLog = tenantAppBizLog((CairoOAuthTenantAppUserAuthenticationToken) authentication, bizLog, params, errorMessage, ip, startTime, endTime, millis);
					bizLogService.storeTenantAppBizLog(tenantAppBizLog);
				}  else if (authentication instanceof CairoOAuthTenantSubappUserAuthenticationToken) {
					TenantSubappBizLog tenantSubappBizLog = tenantSubappBizLog((CairoOAuthTenantSubappUserAuthenticationToken) authentication, bizLog, params, errorMessage, ip, startTime, endTime, millis);
					bizLogService.storeTenantSubappBizLog(tenantSubappBizLog);
				} else {
					OpenBizLog openBizLog = openBizLog(bizLog, params, errorMessage, ip, startTime, endTime, millis);
					bizLogService.storeOpenBizLog(openBizLog);
				}
			} catch (Exception e) {
				log.info("bizLogAspect: ", e);
			}
		}

		return result;
	}

	protected OpenBizLog openBizLog(BizLog bizLog, String params, String errorMessage, String ip, LocalDateTime startTime, LocalDateTime endTime, long mills) {
		return OpenBizLog.builder()
			.logId(CoreConstants.nextIdStr())
			.appId(bizLogService.getAppId())
			.bizId(bizLog.bizId())
			.scope(bizLog.scope())
			.params(params)
			.success(errorMessage == null)
			.errorMessage(errorMessage)
			.ip(ip)
			.startTime(startTime)
			.endTime(endTime)
			.mills(mills)
			.build();
	}

	protected ClientBizLog clientBizLog(CairoOAuthClientAuthenticationToken token, BizLog bizLog, String params, String errorMessage, String ip, LocalDateTime startTime, LocalDateTime endTime, long mills) {
		CairoOAuthClientPrincipal principal = token.getPrincipal();
		return ClientBizLog.builder()
			.logId(CoreConstants.nextIdStr())
			.appId(principal.getAppId())
			.clientId(principal.getClientId())
			.clientTokenId(principal.getId())
			.bizId(bizLog.bizId())
			.scope(bizLog.scope())
			.params(params)
			.success(errorMessage == null)
			.errorMessage(errorMessage)
			.ip(ip)
			.startTime(startTime)
			.endTime(endTime)
			.mills(mills)
			.build();
	}

	protected AccountBizLog accountBizLog(CairoOAuthAccountAuthenticationToken token, BizLog bizLog, String params, String errorMessage, String ip, LocalDateTime startTime, LocalDateTime endTime, long mills) {
		CairoOAuthAccountPrincipal principal = token.getPrincipal();
		return AccountBizLog.builder()
			.logId(CoreConstants.nextIdStr())
			.clientId(principal.getClientId())
			.appId(principal.getAppId())
			.clientId(principal.getClientId())
			.accountId(principal.getAccountId())
			.tokenId(principal.getId())
			.bizId(bizLog.bizId())
			.scope(bizLog.scope())
			.params(params)
			.success(errorMessage == null)
			.errorMessage(errorMessage)
			.ip(ip)
			.startTime(startTime)
			.endTime(endTime)
			.mills(mills)
			.build();
	}

	protected AppBizLog endpointBizLog(CairoOAuthAppUserAuthenticationToken token, BizLog bizLog, String params, String errorMessage, String ip, LocalDateTime startTime, LocalDateTime endTime, long mills) {
		CairoOAuthAppUserPrincipal principal = token.getPrincipal();
		return AppBizLog.builder()
			.logId(CoreConstants.nextIdStr())
			.appId(principal.getAppId())
			.endpointId(principal.getEndpointId())
			.clientId(principal.getClientId())
			.userId(principal.getUserId())
			.tokenId(principal.getId())
			.bizId(bizLog.bizId())
			.scope(bizLog.scope())
			.params(params)
			.success(errorMessage == null)
			.errorMessage(errorMessage)
			.ip(ip)
			.startTime(startTime)
			.endTime(endTime)
			.mills(mills)
			.build();
	}

	protected SubappBizLog subappBizLog(CairoOAuthSubappUserAuthenticationToken token, BizLog bizLog, String params, String errorMessage, String ip, LocalDateTime startTime, LocalDateTime endTime, long mills) {
		CairoOAuthSubappUserPrincipal principal = token.getPrincipal();
		return SubappBizLog.builder()
			.logId(CoreConstants.nextIdStr())
			.appId(principal.getAppId())
			.endpointId(principal.getEndpointId())
			.subappId(principal.getSubappId())
			.subappVersion(principal.getSubappVersion())
			.userId(principal.getUserId())
			.tokenId(principal.getId())
			.bizId(bizLog.bizId())
			.scope(bizLog.scope())
			.params(params)
			.success(errorMessage == null)
			.errorMessage(errorMessage)
			.ip(ip)
			.startTime(startTime)
			.endTime(endTime)
			.mills(mills)
			.build();
	}


	protected TenantAppBizLog tenantAppBizLog(CairoOAuthTenantAppUserAuthenticationToken token, BizLog bizLog, String params, String errorMessage, String ip, LocalDateTime startTime, LocalDateTime endTime, long mills) {
		CairoOAuthTenantAppUserPrincipal principal = token.getPrincipal();
		return TenantAppBizLog.builder()
			.logId(CoreConstants.nextIdStr())
			.tenantId(principal.getTenantId())
			.appId(principal.getAppId())
			.endpointId(principal.getEndpointId())
			.clientId(principal.getClientId())
			.userId(principal.getUserId())
			.tokenId(principal.getId())
			.bizId(bizLog.bizId())
			.scope(bizLog.scope())
			.params(params)
			.success(errorMessage == null)
			.errorMessage(errorMessage)
			.ip(ip)
			.startTime(startTime)
			.endTime(endTime)
			.mills(mills)
			.build();
	}

	protected TenantSubappBizLog tenantSubappBizLog(CairoOAuthTenantSubappUserAuthenticationToken token, BizLog bizLog, String params, String errorMessage, String ip, LocalDateTime startTime, LocalDateTime endTime, long mills) {
		CairoOAuthTenantSubappUserPrincipal principal = token.getPrincipal();
		return TenantSubappBizLog.builder()
			.logId(CoreConstants.nextIdStr())
			.tenantId(principal.getTenantId())
			.appId(principal.getAppId())
			.endpointId(principal.getEndpointId())
			.subappId(principal.getSubappId())
			.subappVersion(principal.getSubappVersion())
			.userId(principal.getUserId())
			.tokenId(principal.getId())
			.bizId(bizLog.bizId())
			.scope(bizLog.scope())
			.params(params)
			.success(errorMessage == null)
			.errorMessage(errorMessage)
			.ip(ip)
			.startTime(startTime)
			.endTime(endTime)
			.mills(mills)
			.build();
	}

	@SneakyThrows
	protected String getParams(ProceedingJoinPoint point) {
		Signature signature = point.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;
		String[] parameterNames = methodSignature.getParameterNames();
		Object[] args = point.getArgs();
		Map<String, Object> data = new HashMap<>();
		for (int i = 0; i < parameterNames.length; i++) {
			int finalI = i;
			data.put(parameterNames[i], Optional.ofNullable(args).filter(a -> args.length > finalI).map(a -> a[finalI]).orElse(null));
		}
		return objectMapper.writeValueAsString(data);
	}

	@SneakyThrows
	protected String getParams(JoinPoint pjp, Object result, BizLog log) {
		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		Method method = methodSignature.getMethod();
		Object[] args = pjp.getArgs();

		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < log.params().length; i++) {
			String key = log.params()[i].key();
			String spelValue = log.params()[i].value();
			Object value = SpelParserUtils.parse(method, args, result, spelValue, Object.class);
			map.put(key, value);
		}
		return objectMapper.writeValueAsString(map);
	}
}
