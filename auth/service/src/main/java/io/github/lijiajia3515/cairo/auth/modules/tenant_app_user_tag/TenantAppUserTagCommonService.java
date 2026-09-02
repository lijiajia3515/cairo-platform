package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_tag;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_tag.TenantAppUserTag;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTagMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.BasicTenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserConverter;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * [common_service] tenant app user tag service
 */
@Slf4j
@Component
public class TenantAppUserTagCommonService {

	@Qualifier("readMongoTemplate")
	private final MongoTemplate readMongoTemplate;

	public TenantAppUserTagCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 判断是否存在用户使用tagIds
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param tagIds   tagIds
	 * @return 是否存在用户
	 */
	@NewSpan
	public List<BasicTenantAppUser> existsTenantAppUserList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull @NotEmpty String... tagIds) {
		return existsTenantAppUserList(tenantId, appId, Set.of(tagIds));
	}


	/**
	 * 判断是否存在用户使用tagIds
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param tagIds   tagIds
	 * @return 是否存在用户
	 */
	@NewSpan
	public List<BasicTenantAppUser> existsTenantAppUserList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull @NotEmpty Collection<String> tagIds) {
		final Criteria criteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.TAG_IDS).elemMatch(new Criteria().in(Set.of(tagIds)));
		Query query = Query.query(criteria);
		query.fields().include(TenantAppUserMongodb.FIELD.USER_ID, TenantAppUserMongodb.FIELD.NICKNAME);
		query.limit(10);

		return readMongoTemplate.find(Query.query(criteria), TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER).stream().map(TenantAppUserConverter::convertMetadataUser).collect(Collectors.toList());
	}

	/**
	 * get user tag list by tagIds
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param tagIds   args
	 * @return a user tag list
	 */
	@NewSpan
	public List<TenantAppUserTag> getUserTagListByTagIds(@Valid @NotNull String tenantId, @Valid @NotNull String appId, List<String> tagIds) {

		Criteria criteria = Criteria
			.where(TenantAppUserTagMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserTagMongodb.FIELD.APP_ID).is(appId);

		if (tagIds != null && !tagIds.isEmpty()) {
			criteria.and(TenantAppUserTagMongodb.FIELD.TAG_ID).in(tagIds);
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.desc(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<TenantAppUserTagMongodb> userTags = readMongoTemplate.find(query, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);
		log.debug("[user_tag][get_user_tag_list] query: {}", userTags);

		return getUserTagList(readMongoTemplate, tenantId, appId, userTags);
	}


	@NewSpan
	protected List<TenantAppUserTag> getUserTagList(MongoTemplate template, String tenantId, String appId, List<TenantAppUserTagMongodb> ms) {
		return ms.stream().map(TenantAppUserTagConverter::convertTenantAppUserTag).collect(Collectors.toList());
	}


}
