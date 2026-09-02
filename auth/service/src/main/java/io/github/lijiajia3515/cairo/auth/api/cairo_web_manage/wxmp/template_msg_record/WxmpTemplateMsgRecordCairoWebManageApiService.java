package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.wxmp.template_msg_record;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.api.client.app.AppClientApiService;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg_record.GetWxmpTemplateMsgRecordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg_record.RetryWxmpTemplateMsgRecordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg_record.MetadataWxmpTemplateMsgRecord;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgRecordMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.AsyncSendWxmpMsgService;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.args.SendWxmpMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg.WxmpTemplateMsgCommonService;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg_record.WxmpTemplateMsgRecordConverter;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * [cairo_web_manage/api] wxmp template msg record service
 */
@Slf4j
@Validated
@Component
public class WxmpTemplateMsgRecordCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final AppClientApiService appClientApiService;
	private final ObjectMapper objectMapper;
	private final AsyncSendWxmpMsgService asyncSendWxmpMsgService;
	private final WxmpTemplateMsgCommonService wxmpTemplateMsgCommonService;

	WxmpTemplateMsgRecordCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												  AppUserCommonService appUserCommonService,
												  CairoSecurityProperties cairoSecurityProperties,
												  AppClientApiService appClientApiService,
												  ObjectMapper objectMapper,
												  AsyncSendWxmpMsgService asyncSendWxmpMsgService,
												  WxmpTemplateMsgCommonService wxmpTemplateMsgCommonService) {
		this.appUserCommonService = appUserCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.readMongoTemplate = readMongoTemplate;
		this.appClientApiService = appClientApiService;
		this.objectMapper = objectMapper;
		this.asyncSendWxmpMsgService = asyncSendWxmpMsgService;
		this.wxmpTemplateMsgCommonService = wxmpTemplateMsgCommonService;
	}

	/**
	 * 查询微信模板消息分页列表
	 *
	 * @param appId appId
	 * @param args  query args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "wxms_message:get_wxms_message_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataWxmpTemplateMsgRecord> getWxmpTemplateMsgRecordPageList(@NotNull String appId, @Validated GetWxmpTemplateMsgRecordArgs args) {

		Criteria criteria = Criteria.where(WxmpTemplateMsgRecordMongodb.FIELD.APP_ID).is(appId);
		if (args.getBizId() != null && !args.getBizId().isBlank()) {
			criteria.and(WxmpTemplateMsgRecordMongodb.FIELD.BIZ_ID).is(args.getBizId());
		}

		if (args.getSuccess() != null) {
			criteria.and(WxmpTemplateMsgRecordMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(WxmpTemplateMsgRecordMongodb.FIELD.TEXT).regex(args.getKeyword()),
				Criteria.where(WxmpTemplateMsgRecordMongodb.FIELD.OPEN_ID).regex(args.getKeyword()),
				Criteria.where(WxmpTemplateMsgRecordMongodb.FIELD.REASON).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);
		long total = readMongoTemplate.count(query, WxmpTemplateMsgRecordMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_RECORD);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(WxmpTemplateMsgRecordMongodb.FIELD.TIME)));

		List<WxmpTemplateMsgRecordMongodb> wxmsMessageMongodbList = readMongoTemplate.find(query, WxmpTemplateMsgRecordMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_RECORD);
		List<MetadataWxmpTemplateMsgRecord> contents = getMetadataWxmpTemplateMsgRecordList(wxmsMessageMongodbList);
		return new Page<>(args, contents, total);
	}

	/**
	 * 重试微信消息
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "wxms_message:retry_wxms_message",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void retryWxmpTemplateMsgRecord(@NotNull String appId, @Validated RetryWxmpTemplateMsgRecordArgs args) {
		Criteria criteria = Criteria
			.where(WxmpTemplateMsgRecordMongodb.FIELD.APP_ID).is(appId)
			.and(WxmpTemplateMsgRecordMongodb.FIELD.MSG_ID).is(args.getMsgId());
		Query query = Query.query(criteria);
		WxmpTemplateMsgRecordMongodb wxmsMessageMongodb = readMongoTemplate.findOne(query, WxmpTemplateMsgRecordMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_RECORD);
		if (wxmsMessageMongodb == null) {
			throw new ConflictBusinessException("微信消息不存在");
		}

		if (wxmsMessageMongodb.isSuccess()) {
			throw new ConflictBusinessException("已成功发送，无需重试");
		}

		if (wxmsMessageMongodb.getVersion() > 3L) {
			throw new ConflictBusinessException("重试次数过多");
		}

		Map<String, SendWxmpMsgByArgs.MessageContent> bizArgs = null;
		if (wxmsMessageMongodb.getBizArgs() != null) {
			bizArgs = objectMapper.readValue(wxmsMessageMongodb.getBizArgs(), new TypeReference<Map<String, SendWxmpMsgByArgs.MessageContent>>() {
			});
		}

		// 异步发送消息
		WxmpTemplateMsg wxmpTemplateMsg = wxmpTemplateMsgCommonService.getWxmpTemplateMsg(appId, wxmsMessageMongodb.getBizId());
		asyncSendWxmpMsgService.send(SendWxmpMsgArgs.builder()
			.msgId(wxmsMessageMongodb.getMsgId())
			.appId(wxmsMessageMongodb.getAppId())
			.openId(wxmsMessageMongodb.getOpenId())
			.params(bizArgs)
			.jumpUrl(wxmsMessageMongodb.getJumpUrl())
			.wxmpTemplateMsg(wxmpTemplateMsg)
			.snsProviderId(wxmsMessageMongodb.getWxmpProviderId())
			.build());
	}

	private List<MetadataWxmpTemplateMsgRecord> getMetadataWxmpTemplateMsgRecordList(List<WxmpTemplateMsgRecordMongodb> mongodbList) {
		Set<String> userIds = mongodbList.stream().map(WxmpTemplateMsgRecordMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());

		// appMap
		Map<String, App> appMap;
		List<String> appIds = mongodbList.stream().map(WxmpTemplateMsgRecordMongodb::getAppId).distinct().collect(Collectors.toList());
		List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
			.appIds(appIds)
			.build());
		if (!appIds.isEmpty()) {
			appMap = Optional.ofNullable(appList).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(App::getAppId, g -> g));
		} else {
			appMap = Collections.emptyMap();
		}
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);

		return mongodbList.stream()
			.map(x -> WxmpTemplateMsgRecordConverter.convertMetadataWxmpTemplateMsgRecord(x, appMap, metadataUserMap))
			.collect(Collectors.toList());
	}

}
