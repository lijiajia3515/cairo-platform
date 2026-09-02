package io.github.lijiajia3515.cairo.auth.modules.permission;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Component
public class PermissionCommonService {
	private final MongoTemplate readMongoTemplate;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;

	public PermissionCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate, MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate) {
		this.readMongoTemplate = readMongoTemplate;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
	}

	/**
	 * 是否含有功能权限
	 *
	 * @param appId         appId
	 * @param endpointId endpointId
	 * @param menuIds       菜单数组
	 * @return 是否包含
	 */
	public List<Permission> existsPermissionList(MongoTemplate mongoTemplate, @Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Valid String... menuIds) {
		Query deletePermissionQuery = Query.query(Criteria
			.where(PermissionMongodb.FIELD.APP_ID).is(appId)
			.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(PermissionMongodb.FIELD.MENU_ID).in(Set.of(menuIds))
		);
		deletePermissionQuery.fields().include(PermissionMongodb.FIELD.PERMISSION_ID, PermissionMongodb.FIELD.PERMISSION_NAME);
		deletePermissionQuery.limit(10);

		return mongoTemplate.find(deletePermissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION).stream()
			.map(x -> PermissionConverter.convertPermission(x, Collections.emptyMap())).collect(Collectors.toList());
	}
}
