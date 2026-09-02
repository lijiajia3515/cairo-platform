package io.github.lijiajia3515.cairo.auth.modules.app_user_tag;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_tag.AppUserTag;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserTagMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.BasicAppUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * [common_service] app user tag service
 */
@Slf4j
@Component
public class AppUserTagCommonService {

	@Qualifier("readMongoTemplate")
	private final MongoTemplate readMongoTemplate;

	public AppUserTagCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 判断是否存在应用用户使用tagIds
	 *
	 * @param appId    appId
	 * @param tagIds   tagIds
	 * @return 是否存在用户
	 */
	@NewSpan
	public List<BasicAppUser> existsAppUserList(@Valid @NotNull String appId, @Valid @NotNull @NotEmpty String... tagIds) {
		return existsAppUserList(appId, Set.of(tagIds));
	}


	/**
	 * 判断是否存在应用用户使用tagIds
	 *
	 * @param appId    appId
	 * @param tagIds   tagIds
	 * @return 是否存在用户
	 */
	@NewSpan
	public List<BasicAppUser> existsAppUserList(@Valid @NotNull String appId, @Valid @NotNull @NotEmpty Collection<String> tagIds) {
		final Criteria criteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.TAG_IDS).elemMatch(new Criteria().in(Set.of(tagIds)));
		Query query = Query.query(criteria);
		query.fields().include(AppUserMongodb.FIELD.USER_ID, AppUserMongodb.FIELD.NICKNAME);
		query.limit(10);

		return readMongoTemplate.find(Query.query(criteria), AppUserMongodb.class, MongodbConstants.Collection.APP_USER).stream()
			.map(AppUserConverter::convertBasicAppUser)
			.collect(Collectors.toList());
	}



	/**
	 * get app_user tag list by tagIds
	 *
	 * @param appId    appId
	 * @param tagIds   args
	 * @return a user tag list
	 */
	@NewSpan
	public List<AppUserTag> getAppUserTagListByTagIds(@Valid @NotNull String appId, Collection<String> tagIds) {

		Criteria criteria = Criteria
			.where(AppUserTagMongodb.FIELD.APP_ID).is(appId);

		if (tagIds != null && !tagIds.isEmpty()) {
			criteria.and(AppUserTagMongodb.FIELD.TAG_ID).in(tagIds);
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.desc(AppUserTagMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<AppUserTagMongodb> userTags = readMongoTemplate.find(query, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);
		log.debug("[app_user_tag][get_app_user_tag_list] query: {}", userTags);

		return getAppUserTagList(readMongoTemplate, appId, userTags);
	}

	@NewSpan
	protected List<AppUserTag> getAppUserTagList(MongoTemplate template, String appId, List<AppUserTagMongodb> ms) {
		return ms.stream().map(AppUserTagConverter::convertAppUserTag).collect(Collectors.toList());
	}

}
