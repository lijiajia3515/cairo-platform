package io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgArgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgMongodb;
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

import java.util.Collections;
import java.util.List;

@Slf4j
@Validated
@Component
public class WxmpTemplateMsgCommonService {
	private final MongoTemplate readMongoTemplate;

	public WxmpTemplateMsgCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	@NewSpan
	@BizLog(
		bizId = "wxmp_template_msg:get_wxmp_template_msg",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public WxmpTemplateMsg getWxmpTemplateMsg(@Valid @NotNull String appId, @Valid @NotNull String bizId) {
		Criteria wxmsTemplateCriteria = Criteria
			.where(WxmpTemplateMsgMongodb.FIELD.APP_ID).is(appId)
			.and(WxmpTemplateMsgMongodb.FIELD.BIZ_ID).is(bizId)
			.and(WxmpTemplateMsgMongodb.FIELD.ENABLED).is(true);
		Query wxmsTemplateQuery = Query.query(wxmsTemplateCriteria);
		WxmpTemplateMsgMongodb wxmsTemplateMongodb = readMongoTemplate.findOne(wxmsTemplateQuery, WxmpTemplateMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG);
		if (wxmsTemplateMongodb == null) {
			return null;
		}
		Criteria wxmsTemplateArgCriteria = Criteria
			.where(WxmpTemplateMsgArgMongodb.FIELD.APP_ID).is(appId)
			.and(WxmpTemplateMsgArgMongodb.FIELD.BIZ_ID).is(bizId);
		Query wxmsTemplateArgQuery = Query.query(wxmsTemplateArgCriteria);
		wxmsTemplateArgQuery.with(Sort.by(WxmpTemplateMsgArgMongodb.FIELD.SORT));
		List<WxmpTemplateMsgArgMongodb> wxmsTemplateArgMongodbList = readMongoTemplate.find(wxmsTemplateArgQuery, WxmpTemplateMsgArgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_ARGS);
		return WxmpTemplateMsgConverter.convertWxmpTemplateMsg(
			wxmsTemplateMongodb,
			Collections.emptyMap(),
			Collections.singletonMap(bizId, wxmsTemplateArgMongodbList)
		);

	}
}
