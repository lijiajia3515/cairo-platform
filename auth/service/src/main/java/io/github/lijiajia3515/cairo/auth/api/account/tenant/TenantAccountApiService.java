package io.github.lijiajia3515.cairo.auth.api.account.tenant;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [account/api] tenant service
 */
@Slf4j
@Validated
@Component
public class TenantAccountApiService {
	private final MongoTemplate readMongoTemplate;
	private final AccountCommonService accountCommonService;

	public TenantAccountApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								   AccountCommonService accountCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.accountCommonService = accountCommonService;
	}

	/**
	 * 获取我的企业
	 *
	 * @param accountId 账号id
	 * @return 企业列表
	 */
	@BizLog(
		bizId = "tenant:get_my_tenant",
		scope = "read",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId")
		}
	)
	@NewSpan
	public List<Tenant> getMyTenantList(@Valid @NotNull String accountId) {
		// tenant query
		Criteria tenantCriteria = Criteria.where(TenantMongodb.FIELD.OWNER_ACCOUNT_ID).is(accountId);
		Query tenantQuery = Query.query(tenantCriteria);
		tenantQuery.with(Sort.by(Sort.Order.desc(TenantMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantMongodb> tenantMongodbList = readMongoTemplate.find(tenantQuery, TenantMongodb.class, MongodbConstants.Collection.TENANT);

		List<String> ownerAccountIds = tenantMongodbList.stream().map(TenantMongodb::getOwnerAccountId).collect(Collectors.toList());
		Map<String, Account> ownerAccountMap = accountCommonService.getAccountMapByAccountIds(ownerAccountIds);

		return tenantMongodbList.stream()
			.map(x -> Tenant.builder()
				.tenantId(x.getTenantId())
				.tenantName(x.getTenantName())
				.enabled(x.getEnabled())
				.ownerAccount(
					Optional.ofNullable(ownerAccountMap.get(x.getOwnerAccountId()))
						.orElse(Account.builder()
							.accountId(x.getOwnerAccountId())
							.nickname(x.getOwnerAccountId())
							.build())
				)
				.build())
			.collect(Collectors.toList());
	}
}
