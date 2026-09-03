package io.github.lijiajia3515.cairo.auth.modules.app_user;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
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
 * [common_service] app_app_user service
 */
@Slf4j
@Validated
@Component
public class AppUserCommonService {
	private static final String SERIAL_NAMESPACE = "default";
	private static final String SERIAL_KEY = "app_user";

	private final SerialService serialService;

	private final MongoTemplate readMongoTemplate;

	public AppUserCommonService(SerialService serialService, @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.serialService = serialService;
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 不参与其他事务，如果失败返回雪花id
	 *
	 * @return 新的用户id
	 */
	public String getNewAppUserId() {
		try {
			return String.valueOf(serialService.next(SERIAL_NAMESPACE, SERIAL_KEY));
		} catch (Exception e) {
			log.warn("getNewAppUserId: ", e);
			return CoreConstants.nextIdStr();
		}
	}

	/**
	 * get app_user list by app_user ids
	 *
	 * @param appId   appId
	 * @param userIds userIds
	 * @return user list
	 */
	@NewSpan
	public List<AppUser> getUserListByAppUserIds(@Valid @NotNull String appId, Collection<String> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.USER_ID).in(userIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(AppUserMongodb.FIELD.NICKNAME)));

		List<AppUserMongodb> appUsers = readMongoTemplate.find(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		List<String> accountIds = appUsers.stream().map(AppUserMongodb::getAccountId).distinct().collect(Collectors.toList());

		Map<String, AccountMongodb> accountMap = new HashMap<>();

		if (!accountIds.isEmpty()) {
			Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).in(accountIds);
			Query accountQuery = Query.query(accountCriteria);

			List<AccountMongodb> accountMongodbList = readMongoTemplate.find(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			accountMap.putAll(accountMongodbList.stream().collect(Collectors.toMap(AccountMongodb::getAccountId, x -> x)));
		}


		return appUsers.stream().map(x -> convert(x, accountMap)).collect(Collectors.toList());
	}

	/**
	 * get app_user map by app_userIds
	 *
	 * @param appId   appId
	 * @param userIds userIds
	 * @return app_user map
	 */
	@NewSpan
	public Map<String, AppUser> getAppUserMapByAppUserIds(@Valid @NotNull String appId, Collection<String> userIds) {
		return getUserListByAppUserIds(appId, userIds).stream()
			.collect(Collectors.toMap(AppUser::getUserId, x -> x, (x1, x2) -> x1));
	}

	public Optional<String> getAccountIdByAppUserId(@Valid @NotNull String appId, @Valid @NotNull String app_userId) {
		Criteria criteria = Criteria.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.USER_ID).is(app_userId);
		Query query = Query.query(criteria);
		query.fields().include(AppUserMongodb.FIELD.ACCOUNT_ID);
		return Optional.ofNullable(readMongoTemplate.findOne(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER)).map(AppUserMongodb::getAccountId);
	}


	public AppUser convert(AppUserMongodb userMongodb, Map<String, AccountMongodb> accountMongodbMap) {
		String accountId = userMongodb.getAccountId();
		AccountMongodb accountMongodb = accountMongodbMap.get(accountId);
		return AppUser.builder()
			.userId(userMongodb.getUserId())
			.nickname(Optional.ofNullable(userMongodb.getNickname()).orElse(Optional.ofNullable(accountMongodb).map(AccountMongodb::getNickname).orElse(null)))
			.accountAvatarUrl(Optional.ofNullable(accountMongodb).map(x -> accountMongodb.getAvatarUrl()).orElse(null))
			.joinTime(userMongodb.getJoinTime())
			.build();
	}
}
