package io.github.lijiajia3515.cairo.auth.modules.notify.template.common;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.notify.NotifyTemplateArgsMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.notify.NotifyTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.category.NotifyCategory;
import io.github.lijiajia3515.cairo.auth.modules.notify.category.NotifyCategoryCommonService;
import io.github.lijiajia3515.cairo.auth.modules.notify.category.args.GetNotifyCategoryArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.template.NotifyTemplate;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Component
public class NotifyTemplateCommonService {
	private final MongoTemplate readMongoTemplate;
	private final NotifyCategoryCommonService categoryCommonService;

	public NotifyTemplateCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													NotifyCategoryCommonService categoryCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.categoryCommonService = categoryCommonService;
	}

	@NewSpan
	@BizLog(
		bizId = "notify_template:get_notify_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "messageCode", value = "#messageCode")
		}
	)
	@Caching(
		cacheable = {
			@Cacheable(cacheNames = CairoAuthRedisConstants.Keys.NOTIFY_TEMPLATE, key = "#appId+ ':'+ #messageCode")
		}
	)
	public NotifyTemplate getNotifyTemplate(@Valid @NotNull String appId, @Valid @NotNull String messageCode) {
		Criteria smsTemplateCriteria = Criteria
			.where(NotifyTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(NotifyTemplateMongodb.FIELD.MESSAGE_CODE).is(messageCode)
			.and(NotifyTemplateMongodb.FIELD.ENABLED).is(true);
		Query smsTemplateQuery = Query.query(smsTemplateCriteria);
		NotifyTemplateMongodb templateMongodb = readMongoTemplate.findOne(smsTemplateQuery, NotifyTemplateMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE);
		Map<String, NotifyCategory> categoryMap = new HashMap<>(1);
		if (templateMongodb.getCategoryId() != null) {
			categoryCommonService.getNotifyCategory(appId, GetNotifyCategoryArgs.builder()
				.categoryIds(List.of(templateMongodb.getCategoryId()))
				.build()
			);
		}
		Criteria templateArgCriteria = Criteria
			.where(NotifyTemplateArgsMongodb.FIELD.APP_ID).is(appId)
			.and(NotifyTemplateArgsMongodb.FIELD.TEMPLATE_ID).is(templateMongodb.getTemplateId());
		Query templateArgQuery = Query.query(templateArgCriteria);
		templateArgQuery.with(Sort.by(NotifyTemplateArgsMongodb.FIELD.SORT));
		List<NotifyTemplateArgsMongodb> templateArgMongodbList = readMongoTemplate.find(templateArgQuery, NotifyTemplateArgsMongodb.class, MongodbConstants.Collection.NOTIFY_TEMPLATE_ARGS);

		return NotifyTemplateConverter.convertNotifyTemplate(templateMongodb, categoryMap, Collections.singletonMap(templateMongodb.getTemplateId(), templateArgMongodbList));
	}

	@Caching(
		evict = {
			@CacheEvict(cacheNames = CairoAuthRedisConstants.Keys.NOTIFY_TEMPLATE, key = "#appId+ ':'+ #messageCode")
		}
	)
	public void clearCache(String appId, String messageCode) {
		log.info("clear notify_template: [appId={}, messageCode={}]", appId, messageCode);
	}
}
