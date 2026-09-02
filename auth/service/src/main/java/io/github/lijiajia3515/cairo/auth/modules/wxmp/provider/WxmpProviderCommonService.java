package io.github.lijiajia3515.cairo.auth.modules.wxmp.provider;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.provider.MetadataWxmpProvider;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpProviderMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgArgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.GetWxmpProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg.WxmpTemplateMsgConverter;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

@Slf4j
@Validated
@Component
public class WxmpProviderCommonService {
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;


	public WxmpProviderCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										 AppUserCommonService appUserCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appUserCommonService = appUserCommonService;
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
		if (wxmsTemplateMongodb != null) {
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
		} else {
			return null;
		}
	}



	/**
	 * 查询微信公众号连接配置列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_provider:get_wxmp_provider_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataWxmpProvider> getWxmpProviderList(String appId, @Validated GetWxmpProviderArgs args) {
		Criteria criteria = new Criteria();

		if (args.getEnabled() != null) {
			criteria.and(WxmpProviderMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getWxmpProviderIds() != null && !args.getWxmpProviderIds().isEmpty()) {
			criteria.and(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).in(args.getWxmpProviderIds());
		}

		if (args.getKeyword() != null) {
			criteria.orOperator(
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_APP_ID).regex(args.getKeyword()),
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_AES_KEY).regex(args.getKeyword()),
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_TOKEN).regex(args.getKeyword()),
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_SECRET).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(WxmpProviderMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<WxmpProviderMongodb> wxmsTemplateMongodbList = readMongoTemplate.find(query, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);
		return getMetadataWxmpProvider(appId, wxmsTemplateMongodbList);
	}

	private List<MetadataWxmpProvider> getMetadataWxmpProvider(String appId, List<WxmpProviderMongodb> mongodbList) {
		Set<String> userIds = mongodbList.stream().map(WxmpProviderMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(appId, userIds);
		return mongodbList.stream()
			.map(x -> WxmpProviderConverter.convertMetadataWxmpProvider(x, metadataUserMap))
			.collect(Collectors.toList());
	}
}
