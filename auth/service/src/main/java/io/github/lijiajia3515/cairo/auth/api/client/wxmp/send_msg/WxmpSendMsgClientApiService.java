package io.github.lijiajia3515.cairo.auth.api.client.wxmp.send_msg;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsType;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsMsgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgRecordMongodb;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.SendWxmpMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg.WxmpTemplateMsgCommonService;
import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.AsyncSendWxmpMsgService.substring;


/**
 * [client/api] wxmp service
 * 发送微信消息
 */
@Slf4j
@Validated
@Component
public class WxmpSendMsgClientApiService {

	private final WxmpTemplateMsgCommonService wxmpTemplateMsgCommonService;
	private final MongoTemplate readMongoTemplate;
	private final WxMpService wxMpService;
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;


	public WxmpSendMsgClientApiService(WxmpTemplateMsgCommonService wxmpTemplateMsgCommonService,
										   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										   WxMpService wxMpService,
										   @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										   ObjectMapper objectMapper) {
		this.wxmpTemplateMsgCommonService = wxmpTemplateMsgCommonService;
		this.readMongoTemplate = readMongoTemplate;
		this.wxMpService = wxMpService;
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@NewSpan
	@BizLog(
		bizId = "wxmp:send_msg_by_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void sendWxmpMsgByAppUser(String appId, SendWxmpMsgByArgs args) {
		try {
			//获取模板
			WxmpTemplateMsg wxmpTemplateMsg = wxmpTemplateMsgCommonService.getWxmpTemplateMsg(appId, args.getBizId());

			//查询用户连接信息
			Criteria criteria = Criteria.where(WxmpAppUserMongodb.FIELD.WX_PROVIDER_ID).is(wxmpTemplateMsg == null ? "default" : wxmpTemplateMsg.getWxmpProviderId())
				.and(WxmpAppUserMongodb.FIELD.USER_ID).in(args.getToAppUserIds());
			Map<String, WxmpAppUserMongodb> userWxmpSnsMongodbMap = readMongoTemplate.find(Query.query(criteria), WxmpAppUserMongodb.class, MongodbConstants.Collection.WXMP_APP_USER)
				.stream().collect(Collectors.toMap(WxmpAppUserMongodb::getUserId, x -> x));


			args.getToAppUserIds().forEach(userId -> {
				// 获取用户微信号
				WxmpAppUserMongodb userWxmpSnsMongodb = userWxmpSnsMongodbMap.getOrDefault(userId, WxmpAppUserMongodb.builder().build());

				sendWxmpMsg(SendWxmpMsgArgs.builder()
					.appId(appId)
					.bizId(args.getBizId())
					.openId(userWxmpSnsMongodb.getOpenId())
					.params(args.getParams())
					.jumpUrl(args.getJumpUrl())
					.wxmpTemplateMsg(wxmpTemplateMsg)
					.snsProviderId(wxmpTemplateMsg == null ? null : wxmpTemplateMsg.getWxmpProviderId())
					.source(String.format("appUser:%s%s", appId, userId))
					.build());
			});
		} catch (Exception e) {
			log.warn("sendWxmpTemplateMsgByAppUser: ", e);
		}
	}


	@NewSpan
	@BizLog(
		bizId = "wxmp:send_wxmp_message",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void sendWxmpMsg(SendWxmpMsgArgs args) {
		try {
			log.info("send wx_message args{}", args);
			String reason = null;
			String bizArgs = null;
			String providerMsgId = null;
			String providerArgsStr = null;
			Map<String, SendWxmpMsgByArgs.MessageContent> templateArgMap = Collections.emptyMap();
			try {

				bizArgs = objectMapper.writeValueAsString(args.getParams());

				if (args.getWxmpTemplateMsg() == null) {
					reason = String.format("消息模板不存在： AppId: %s BizId: %s", args.getAppId(), args.getBizId());
					return;
				}
				if (args.getOpenId() == null) {
					log.info("openId为空, 忽略此次请求");
					reason = "openId为空，忽略此次请求";
					return;
				}
				if (args.getSnsProviderId() == null) {
					log.info("公众号管理Id为空, 忽略此次请求");
					reason = "公众号管理Id为空，忽略此次请求";
					return;
				}


				// 模板参数map
				templateArgMap = Optional.ofNullable(args.getWxmpTemplateMsg().getArgs()).orElse(Collections.emptyList())
					.stream()
					.map(templateArg -> {
						SendWxmpMsgByArgs.MessageContent templateArgValue = args.getParams().getOrDefault(templateArg.getArgCode(), SendWxmpMsgByArgs.MessageContent.builder().content("").color("").build());
						return Tuple.tuple(templateArg.getTemplateArgCode(), templateArgValue);
					})
					.collect(Collectors.toMap(Tuple2::getV1, Tuple2::getV2, (x1, x2) -> x1));
				// 模板参数
				providerArgsStr = objectMapper.writeValueAsString(templateArgMap);


				List<WxMpTemplateData> data = args.getWxmpTemplateMsg().getArgs().stream().map(x -> {
					SendWxmpMsgByArgs.MessageContent messageContent = args.getParams().getOrDefault(x.getArgCode(), SendWxmpMsgByArgs.MessageContent.builder().build());
					return new WxMpTemplateData(x.getTemplateArgCode(), substring(Optional.ofNullable(messageContent.getContent()).orElse("")), substring(Optional.ofNullable(messageContent.getColor()).orElse(x.getDefaultColor())));
				}).collect(Collectors.toList());

				providerMsgId = wxMpService.switchoverTo(args.getSnsProviderId()).getTemplateMsgService().sendTemplateMsg(WxMpTemplateMessage.builder()
					.templateId(args.getWxmpTemplateMsg().getTemplateCode())
					.toUser(args.getOpenId())
					.data(data)
					.url(args.getJumpUrl() == null ? args.getWxmpTemplateMsg().getJumpUrl() : args.getJumpUrl())
					.build());
				log.info("wx sendMsg success msgId: {}", providerMsgId);


			} catch (Exception e) {
				log.warn("发送微信模板消息失败", e);
				reason = e.getMessage();

			} finally {
				AtomicReference<String> text = new AtomicReference<>(Optional.ofNullable(args.getWxmpTemplateMsg()).map(WxmpTemplateMsg::getTemplateText).orElse(""));
				if (!text.get().isEmpty()) {
					if (args.getParams() != null) {
						args.getParams().forEach((key, value) -> text.set(text.get().replace("${" + key + "}",
							Optional.ofNullable(value).map(SendWxmpMsgByArgs.MessageContent::getContent).orElse(""))));
					}
				}

				Criteria smsMsgCriteria = Criteria.where(WxmpTemplateMsgRecordMongodb.FIELD.MSG_ID).is(args.getMsgId());
				Query smsMsgQuery = Query.query(smsMsgCriteria);
				boolean exists = mongoTemplate.exists(smsMsgQuery, WxmpTemplateMsgRecordMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_RECORD);
				if (!exists) {
					WxmpTemplateMsgRecordMongodb insert = WxmpTemplateMsgRecordMongodb.builder()
						.msgId(args.getMsgId())
						.wxmpProviderId(args.getSnsProviderId())
						.time(LocalDateTime.now())
						.appId(args.getAppId())
						.bizId(args.getWxmpTemplateMsg().getBizId())
						.openId(args.getOpenId())
						.text(text.get())
						.bizArgs(bizArgs)
						.jumpUrl(args.getJumpUrl() == null ? args.getWxmpTemplateMsg().getJumpUrl() : args.getJumpUrl())
						.source(args.getSource())
						.providerType(SnsType.WX_MP.getTypeValue())
						.providerMsgId(providerMsgId)
						.providerTemplateCode(args.getWxmpTemplateMsg().getTemplateCode())
						.success(reason == null)
						.reason(reason)
						.providerArgs(providerArgsStr)
						.build();
					mongoTemplate.insert(insert, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_RECORD);
				} else {
					// appId,bizId, 不变更
					Update update = Update.update(WxmpTemplateMsgRecordMongodb.FIELD.TIME, LocalDateTime.now());
					update.set(WxmpTemplateMsgRecordMongodb.FIELD.PROVIDER_MSG_ID, providerMsgId);
					update.set(WxmpTemplateMsgRecordMongodb.FIELD.SUCCESS, reason == null);
					update.set(WxmpTemplateMsgRecordMongodb.FIELD.REASON, reason);
					update.set(WxmpTemplateMsgRecordMongodb.FIELD.TEXT, text.get());
					update.set(WxmpTemplateMsgRecordMongodb.FIELD.BIZ_ARGS, bizArgs);
					update.inc(WxmpTemplateMsgRecordMongodb.FIELD.VERSION);
					mongoTemplate.updateFirst(smsMsgQuery, update, SmsMsgMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_RECORD);
				}
			}

		} catch (Exception e) {
			log.warn("sendWxmpTemplateMsgByAppUser: ", e);
		}
	}
}
