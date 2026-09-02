package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
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
 * [common_service] tenant app user service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserCommonService {

	private static final String SERIAL_NAMESPACE = "default";
	private static final String SERIAL_KEY = "tenant_app_user";

	private final SerialService serialService;

	private final MongoTemplate readMongoTemplate;

	public TenantAppUserCommonService(SerialService serialService, @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.serialService = serialService;
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 不参与其他事务，如果失败返回雪花id
	 *
	 * @return 新的用户id
	 */
	public String getNewUserId() {
		try {
			return String.valueOf(serialService.nextStr(SERIAL_NAMESPACE, SERIAL_KEY,1,2001));
		} catch (Exception e) {
			log.warn("getNewUserId: ", e);
			return CoreConstants.SNOWFLAKE.nextIdStr();
		}
	}

	/**
	 * get user list by user ids
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param userIds  userIds
	 * @return user list
	 */
	@NewSpan
	public List<TenantAppUser> getUserListByUserIds(@Valid @NotNull String tenantId, @Valid @NotNull String appId, Collection<String> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).in(userIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(TenantAppUserMongodb.FIELD.NICKNAME)));

		List<TenantAppUserMongodb> users = readMongoTemplate.find(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

		List<String> accountIds = users.stream().map(TenantAppUserMongodb::getAccountId).distinct().collect(Collectors.toList());

		Map<String, AccountMongodb> accountMap = new HashMap<>();

		if (!accountIds.isEmpty()) {
			Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).in(accountIds);
			Query accountQuery = Query.query(accountCriteria);

			List<AccountMongodb> accountMongodbList = readMongoTemplate.find(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			accountMap.putAll(accountMongodbList.stream().collect(Collectors.toMap(AccountMongodb::getAccountId, x -> x)));
		}


		return users.stream().map(x -> convert(x, accountMap)).collect(Collectors.toList());
	}

	/**
	 * get user map by userIds
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param userIds  userIds
	 * @return user map
	 */
	@NewSpan
	public Map<String, TenantAppUser> getUserMapByUserIds(@Valid @NotNull String tenantId, @Valid @NotNull String appId, Collection<String> userIds) {
		return getUserListByUserIds(tenantId, appId, userIds).stream()
			.collect(Collectors.toMap(TenantAppUser::getUserId, x -> x, (x1, x2) -> x1));
	}

	public Optional<String> getAccountIdByUserId(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId) {
		Criteria criteria = Criteria.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);
		Query query = Query.query(criteria);
		query.fields().include(TenantAppUserMongodb.FIELD.ACCOUNT_ID);
		return Optional.ofNullable(readMongoTemplate.findOne(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER)).map(TenantAppUserMongodb::getAccountId);
	}


	public TenantAppUser convert(TenantAppUserMongodb userMongodb, Map<String, AccountMongodb> accountMongodbMap) {
		String accountId = userMongodb.getAccountId();
		AccountMongodb accountMongodb = accountMongodbMap.get(accountId);
		return TenantAppUser.builder()
			.userId(userMongodb.getUserId())
			.nickname(Optional.ofNullable(userMongodb.getNickname()).orElse(Optional.ofNullable(accountMongodb).map(AccountMongodb::getNickname).orElse(null)))
			.accountAvatarUrl(Optional.ofNullable(accountMongodb).map(x -> accountMongodb.getAvatarUrl()).orElse(null))
			.joinTime(userMongodb.getJoinTime())
			.build();
	}
}
