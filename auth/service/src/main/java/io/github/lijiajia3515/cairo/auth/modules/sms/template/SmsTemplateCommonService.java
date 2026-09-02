package io.github.lijiajia3515.cairo.auth.modules.sms.template;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.SmsTemplate;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsTemplateArgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsTemplateMongodb;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

@Slf4j
@Validated
@Component
public class SmsTemplateCommonService {
	private final MongoTemplate readMongoTemplate;

	public SmsTemplateCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	@NewSpan
	@BizLog(
		bizId = "sms_template:get_sms_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "bizId", value = "#bizId")
		}
	)
	@Caching(
		cacheable = {
			@Cacheable(cacheNames = CairoAuthRedisConstants.Keys.SMS_TEMPLATE, key = "#appId+ ':'+ #bizId")
		}
	)
	public SmsTemplate getSmsTemplate(@Valid @NotNull String appId, @Valid @NotNull String bizId) {
		Criteria smsTemplateCriteria = Criteria
			.where(SmsTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(SmsTemplateMongodb.FIELD.BIZ_ID).is(bizId)
			.and(SmsTemplateMongodb.FIELD.ENABLED).is(true);
		Query smsTemplateQuery = Query.query(smsTemplateCriteria);
		SmsTemplateMongodb smsTemplateMongodb = readMongoTemplate.findOne(smsTemplateQuery, SmsTemplateMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE);
		Criteria smsTemplateArgCriteria = Criteria
			.where(SmsTemplateArgMongodb.FIELD.APP_ID).is(appId)
			.and(SmsTemplateArgMongodb.FIELD.BIZ_ID).is(bizId);
		Query smsTemplateArgQuery = Query.query(smsTemplateArgCriteria);
		smsTemplateArgQuery.with(Sort.by(SmsTemplateArgMongodb.FIELD.SORT));
		List<SmsTemplateArgMongodb> smsTemplateArgMongodbList = readMongoTemplate.find(smsTemplateArgQuery, SmsTemplateArgMongodb.class, MongodbConstants.Collection.SMS_TEMPLATE_ARG);
		return SmsTemplateConverter.convertSmsTemplate(
			smsTemplateMongodb,
			Collections.emptyMap(),
			Collections.singletonMap(bizId, smsTemplateArgMongodbList)
		);
	}
}
