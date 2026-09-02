package io.github.lijiajia3515.cairo.auth.api.open.link;


import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.LinkMongodb;
import io.github.lijiajia3515.cairo.auth.modules.link.LinkProperties;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * [open/api] link service
 */
@Slf4j
@Validated
@Component
public class LinkOpenApiService {
	private final MongoTemplate readMongoTemplate;

	private final LinkProperties linkProperties;

	LinkOpenApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
					   LinkProperties linkProperties) {
		this.readMongoTemplate = readMongoTemplate;
		this.linkProperties = linkProperties;
	}


	/**
	 * 获取短链url
	 *
	 * @param linkId 短链ID
	 * @return 链接地址
	 */
	@NewSpan
	@BizLog(
		bizId = "link:get_link_url",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public String getLinkUrl(String linkId) {
		Criteria criteria = Criteria.where(LinkMongodb.FIELD.LINK_ID).is(linkId).and(LinkMongodb.FIELD.ENABLED).is(true);
		Query query = Query.query(criteria);
		query.fields().include(LinkMongodb.FIELD.LINK_ID, LinkMongodb.FIELD.LINK_URL, LinkMongodb.FIELD.ACCESS_COUNT);
		Update update = new Update();
		update.inc(LinkMongodb.FIELD.ACCESS_COUNT, 1D);
		update.currentDate(LinkMongodb.FIELD.LAST_ACCESS_TIME);
		FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
		LinkMongodb one = readMongoTemplate.findAndModify(query, update, options, LinkMongodb.class, MongodbConstants.Collection.LINK);
		return Optional.ofNullable(one).map(LinkMongodb::getLinkUrl).orElse(linkProperties.getDefaultUrl());
	}

}
