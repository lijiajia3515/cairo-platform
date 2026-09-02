package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplate;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTemplateMongodb;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.mongodb.serial.SerialService;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * [common_service] tenant_app_user_template service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserTemplateCommonService {
	private static final String SERIAL_NAMESPACE = "default";
	private static final String SERIAL_KEY = "tenant_app_user_template";

	private final SerialService serialService;

	private final MongoTemplate readMongoTemplate;

	public TenantAppUserTemplateCommonService(SerialService serialService, @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.serialService = serialService;
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 不参与其他事务，如果失败返回雪花id
	 *
	 * @return 新的用户id
	 */
	public String getNewTenantAppUserTemplateId() {
		try {
			return String.valueOf(serialService.next(SERIAL_NAMESPACE, SERIAL_KEY,1,1001));
		} catch (Exception e) {
			log.warn("getNewTenantAppUserTemplateId: ", e);
			return CoreConstants.SNOWFLAKE.nextIdStr();
		}
	}

	/**
	 * get tenant_app_user_template list by tenant_app_user_template ids
	 *
	 * @param appId   appId
	 * @param userIds userIds
	 * @return user list
	 */
	@NewSpan
	public List<TenantAppUserTemplate> getUserListByTenantAppUserTemplateIds(@Valid @NotNull String appId, Collection<String> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = Criteria
			.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).in(userIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(TenantAppUserTemplateMongodb.FIELD.NICKNAME)));

		List<TenantAppUserTemplateMongodb> tenantAppUserTemplateMongodbs = readMongoTemplate.find(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.APP_USER);

		List<String> accountIds = tenantAppUserTemplateMongodbs.stream().map(TenantAppUserTemplateMongodb::getAccountId).distinct().collect(Collectors.toList());

		Map<String, AccountMongodb> accountMap = new HashMap<>();

		if (!accountIds.isEmpty()) {
			Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).in(accountIds);
			Query accountQuery = Query.query(accountCriteria);

			List<AccountMongodb> accountMongodbList = readMongoTemplate.find(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			accountMap.putAll(accountMongodbList.stream().collect(Collectors.toMap(AccountMongodb::getAccountId, x -> x)));
		}


		return tenantAppUserTemplateMongodbs.stream().map(x -> convert(x, accountMap)).collect(Collectors.toList());
	}

	/**
	 * get tenant_app_user_template map by tenant_app_user_templateIds
	 *
	 * @param appId   appId
	 * @param userIds userIds
	 * @return tenant_app_user_template map
	 */
	@NewSpan
	public Map<String, TenantAppUserTemplate> getTenantAppUserTemplateMapByTenantAppUserTemplateIds(@Valid @NotNull String appId, Collection<String> userIds) {
		return getUserListByTenantAppUserTemplateIds(appId, userIds).stream()
			.collect(Collectors.toMap(TenantAppUserTemplate::getTenantAppUserTemplateId, x -> x, (x1, x2) -> x1));
	}

	public Optional<String> getAccountIdByTenantAppUserTemplateId(@Valid @NotNull String appId, @Valid @NotNull String tenant_app_user_templateId) {
		Criteria criteria = Criteria.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).is(tenant_app_user_templateId);
		Query query = Query.query(criteria);
		query.fields().include(TenantAppUserTemplateMongodb.FIELD.ACCOUNT_ID);
		return Optional.ofNullable(readMongoTemplate.findOne(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.APP_USER)).map(TenantAppUserTemplateMongodb::getAccountId);
	}


	public TenantAppUserTemplate convert(TenantAppUserTemplateMongodb userMongodb, Map<String, AccountMongodb> accountMongodbMap) {
		String accountId = userMongodb.getAccountId();
		AccountMongodb accountMongodb = accountMongodbMap.get(accountId);
		return TenantAppUserTemplate.builder()
			.tenantAppUserTemplateId(userMongodb.getTenantAppUserTemplateId())
			.nickname(Optional.ofNullable(userMongodb.getNickname()).orElse(Optional.ofNullable(accountMongodb).map(AccountMongodb::getNickname).orElse(null)))
			.accountAvatarUrl(Optional.ofNullable(accountMongodb).map(x -> accountMongodb.getAvatarUrl()).orElse(null))
			.build();
	}
}
