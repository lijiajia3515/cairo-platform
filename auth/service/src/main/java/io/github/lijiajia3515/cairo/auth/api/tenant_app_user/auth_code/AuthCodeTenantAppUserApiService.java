package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.auth_code;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeModel;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.NewAuthCodeArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.auth_code.RedisAuthCodeServiceImpl;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.auth_code.VerifyPasswordAuthCodeArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

/**
 * [tenant_app_user/api] auth code service
 */
@Slf4j
@Validated
@Component
public class AuthCodeTenantAppUserApiService {

	private final MongoTemplate mongoTemplate;
	private final AccountCommonService accountCommonService;
	private final RedisAuthCodeServiceImpl authCodeService;

	public AuthCodeTenantAppUserApiService(MongoTemplate mongoTemplate, AccountCommonService accountCommonService, RedisAuthCodeServiceImpl authCodeService) {
		this.mongoTemplate = mongoTemplate;
		this.accountCommonService = accountCommonService;
		this.authCodeService = authCodeService;
	}

	@NewSpan
	@BizLog(
		bizId = "auth_code:verify_password",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public AuthCodeModel verifyPassword(@Valid @NotNull String accountId,
										@Valid @NotNull String tenantId,
										@Valid @NotNull String appId,
										@Validated(VerifyPasswordAuthCodeArgs.WebService.class) VerifyPasswordAuthCodeArgs args) {
		Criteria criteria = Criteria
			.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(accountId)
			.and(AccountPasswordMongodb.FIELD.TYPE).is(PasswordType.PASSWORD.getType());

		Query query = Query.query(criteria);
		query.fields().include(AccountPasswordMongodb.FIELD.PASSWORD);

		String accountPassword = Optional.ofNullable(mongoTemplate.findOne(query, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD))
			.map(AccountPasswordMongodb::getPassword)
			.orElseThrow(() -> new ConflictBusinessException("账号异常"));

		boolean matches = accountCommonService.getPasswordEncoder().matches(args.getPassword(), accountPassword);

		if (!matches) {
			throw new ConflictBusinessException("密码错误");
		}

		return authCodeService.generate(
			NewAuthCodeArgs.builder()
				.accountId(accountId)
				.ip(args.getIp())
				.build()
		);
	}
}
