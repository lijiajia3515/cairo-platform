package io.github.lijiajia3515.cairo.auth.modules.app_role;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRole;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.BasicAppUser;
import lombok.extern.slf4j.Slf4j;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Component
public class AppRoleCommonService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;

	private final MongoTemplate readMongoTemplate;

	public AppRoleCommonService(MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate, MongoTemplate readMongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
	}

	@NewSpan
	public List<AppRole> getRoleList(@Valid @NotNull String appId, Collection<String> roleIds) {
		if (roleIds == null || roleIds.isEmpty()) return Collections.emptyList();

		Criteria criteria = Criteria
			.where(AppRoleMongodb.FIELD.APP_ID).is(appId)
			.and(AppRoleMongodb.FIELD.ROLE_ID).in(roleIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppRoleMongodb.FIELD.METADATA.UPDATE_TIME)));

		return readMongoTemplate.find(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE).stream()
			.map(AppRoleConverter::convert).collect(Collectors.toList());
	}

	@NewSpan
	public List<BasicAppUser> existsAppUserList(@Valid @NotNull String appId, @Valid @NotNull @NotEmpty Collection<String> roleIds) {
		final Criteria criteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.ROLE_IDS).in(roleIds);
		Query query = Query.query(criteria);
		query.fields().include(AppUserMongodb.FIELD.USER_ID, AppUserMongodb.FIELD.NICKNAME);
		query.limit(10);
		return readMongoTemplate.find(Query.query(criteria), AppUserMongodb.class, MongodbConstants.Collection.APP_USER).stream()
			.map(AppUserConverter::convertBasicAppUser)
			.collect(Collectors.toList());
	}
}
