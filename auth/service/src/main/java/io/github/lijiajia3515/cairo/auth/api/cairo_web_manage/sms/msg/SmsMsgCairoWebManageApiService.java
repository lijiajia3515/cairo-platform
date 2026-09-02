package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.sms.message;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.api.client.app.AppClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsMsgMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.message.MetadataSmsMsg;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.SmsMsgConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.message.GetSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.message.RetrySmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg.SendMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg.SendMsgSmsService;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.SmsTemplate;
import io.github.lijiajia3515.cairo.auth.modules.sms.template.SmsTemplateCommonService;
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

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

/**
 * [cairo_web_manage/api] sms message service
 */
@Slf4j
@Validated
@Component
public class SmsMsgCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final SmsTemplateCommonService smsTemplateCommonService;
	private final SendMsgSmsService sendMsgSmsService;
	private final AppUserCommonService appUserCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final AppClientApiService appClientApiService;

	private final ObjectMapper objectMapper;
	private final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
		@Override
		public Type getType() {
			return super.getType();
		}
	};

	SmsMsgCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										   SmsTemplateCommonService smsTemplateCommonService,
										   SendMsgSmsService sendMsgSmsService,
										   CairoSecurityProperties cairoSecurityProperties,
										   AppUserCommonService appUserCommonService,
										   AppClientApiService appClientApiService,
										   ObjectMapper objectMapper) {
		this.smsTemplateCommonService = smsTemplateCommonService;
		this.sendMsgSmsService = sendMsgSmsService;
		this.appUserCommonService = appUserCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.readMongoTemplate = readMongoTemplate;
		this.appClientApiService = appClientApiService;
		this.objectMapper = objectMapper;
	}

	/**
	 * 查询短信消息记录分页列表
	 *
	 * @param appId appId
	 * @param args  query args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "sms_msg:get_sms_msg_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataSmsMsg> getSmsMsgPageList(@NotNull String appId, @Validated GetSmsMsgArgs args) {
		Criteria criteria = Criteria.where(SmsMsgMongodb.FIELD.APP_ID).is(appId);
		if (args.getBizId() != null) {
			criteria.and(SmsMsgMongodb.FIELD.BIZ_ID).is(args.getBizId());
		}

		if (args.getPhoneNumber() != null) {
			criteria.and(SmsMsgMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber());
		}

		if (args.getSuccess() != null) {
			criteria.and(SmsMsgMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null) {
			criteria.orOperator(
				Criteria.where(SmsMsgMongodb.FIELD.TEXT).regex(args.getKeyword()),
				Criteria.where(SmsMsgMongodb.FIELD.REASON).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);
		long total = readMongoTemplate.count(query, SmsMsgMongodb.class, MongodbConstants.Collection.SMS_MSG);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(SmsMsgMongodb.FIELD.TIME)));

		List<SmsMsgMongodb> smsMsgMongodbList = readMongoTemplate.find(query, SmsMsgMongodb.class, MongodbConstants.Collection.SMS_MSG);
		List<MetadataSmsMsg> contents = getMetadataSmsMsgList(smsMsgMongodbList);
		return new Page<>(args, contents, total);
	}

	/**
	 * 重试短信消息
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "sms_msg:retry_sms_msg",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void retrySmsMsg(@NotNull String appId, @Validated RetrySmsMsgArgs args) {
		Criteria criteria = Criteria
			.where(SmsMsgMongodb.FIELD.APP_ID).is(appId)
			.and(SmsMsgMongodb.FIELD.MSG_ID).is(args.getMsgId());
		Query query = Query.query(criteria);
		SmsMsgMongodb smsMsgMongodb = readMongoTemplate.findOne(query, SmsMsgMongodb.class, MongodbConstants.Collection.SMS_MSG);
		if (smsMsgMongodb == null) {
			throw new ConflictBusinessException("短信消息不存在");
		}

		if (smsMsgMongodb.isSuccess()) {
			throw new ConflictBusinessException("已成功发送，无需重试");
		}

		if (smsMsgMongodb.getVersion() > 3L) {
			throw new ConflictBusinessException("重试次数过多");
		}

		SmsTemplate smsTemplate = smsTemplateCommonService.getSmsTemplate(smsMsgMongodb.getAppId(), smsMsgMongodb.getBizId());
		Map<String, String> bizArgs = null;
		if (smsMsgMongodb.getBizArgs() != null) {
			bizArgs = objectMapper.readValue(smsMsgMongodb.getBizArgs(), MAP_TYPE);
		}

		// 异步发送消息
		sendMsgSmsService.sendMsg(SendMsgArgs.builder()
			.msgId(smsMsgMongodb.getMsgId())
			.time(smsMsgMongodb.getTime())
			.appId(smsMsgMongodb.getAppId())
			.phoneNumber(smsMsgMongodb.getPhoneNumber())
			.bizId(smsMsgMongodb.getBizId())
			.bizSign(smsMsgMongodb.getProviderSign())
			.bizArgs(bizArgs)
			.smsTemplate(smsTemplate)
			.build()
		);
	}

	private List<MetadataSmsMsg> getMetadataSmsMsgList(List<SmsMsgMongodb> mongodbList) {
		Set<String> userIds = mongodbList.stream().map(SmsMsgMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());


		// appMap
		Map<String, App> appMap;
		List<String> appIds = mongodbList.stream().map(SmsMsgMongodb::getAppId).distinct().collect(Collectors.toList());
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
			.map(x -> SmsMsgConverter.convertMetadataSmsMsg(x, appMap, metadataUserMap))
			.collect(Collectors.toList());
	}

}
