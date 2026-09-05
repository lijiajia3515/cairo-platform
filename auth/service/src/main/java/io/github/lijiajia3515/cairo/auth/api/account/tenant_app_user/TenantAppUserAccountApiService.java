package io.github.lijiajia3515.cairo.auth.api.account.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.account.tenant_app_user.TenantAppUser;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [account/api] tenant app user service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserAccountApiService {
	private final MongoTemplate readMongoTemplate;
	private final TenantCommonService tenantCommonService;

	public TenantAppUserAccountApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate, TenantCommonService tenantCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantCommonService = tenantCommonService;
	}

	/**
	 * 获取我的应用身份列表
	 *
	 * @param appId     应用id
	 * @param accountId 账号id
	 * @return 企业列表
	 */
	@BizLog(
		bizId = "tenant_app_user:get_my_tenant_app_user",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "accountId", value = "#accountId")
		}
	)
	@NewSpan
	public List<TenantAppUser> getMyTenantAppUserList(@Valid @NotNull String appId, @Valid @NotNull String accountId) {
		Criteria userCriteria = Criteria.where(TenantAppUserMongodb.FIELD.APP_ID).is(appId).and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(accountId);
		Query userQuery = Query.query(userCriteria);
		userQuery.fields().include(TenantAppUserMongodb.FIELD.TENANT_ID, TenantAppUserMongodb.FIELD.USER_ID, TenantAppUserMongodb.FIELD.NICKNAME, TenantAppUserMongodb.FIELD.JOIN_TIME);

		List<TenantAppUserMongodb> userList = readMongoTemplate.find(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
		Set<String> tenantIds = userList.stream().map(TenantAppUserMongodb::getTenantId).collect(Collectors.toSet());

		Map<String, Tenant> tenantMap = tenantCommonService.getBasicTenantMapByTenantIds(tenantIds);

		return userList.stream().sorted(Comparator.comparing(TenantAppUserMongodb::getJoinTime).reversed())
			.map(x -> TenantAppUser.builder()
				.tenantId(x.getTenantId())
				.tenantName(Optional.ofNullable(tenantMap.get(x.getTenantId())).map(Tenant::getTenantName).orElse(x.getTenantId()))
				.tenantIcon(Optional.ofNullable(tenantMap.get(x.getTenantId())).map(Tenant::getIcon).orElse(null))
				.userId(x.getUserId())
				.nickname(x.getNickname())
				.joinTime(x.getJoinTime())
				.build())
			.collect(Collectors.toList());
	}
}
