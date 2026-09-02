package io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsType;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsMsgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgRecordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.args.SendWxmpMsgArgs;
import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 异步发送微信消息
 */
@Slf4j
@Validated
@Component
public class AsyncSendWxmpMsgService {
	private final WxMpService wxMpService;
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public AsyncSendWxmpMsgService(WxMpService wxMpService,
									   @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
									   ObjectMapper objectMapper) {
		this.wxMpService = wxMpService;
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@Async
	public void send(SendWxmpMsgArgs sendWxmpMsgArgs) {
        log.info("send wx_message args{}", sendWxmpMsgArgs);
		String reason = null;
		String bizArgs = null;
		String providerMsgId = null;
		String providerArgsStr = null;
		Map<String, SendWxmpMsgByArgs.MessageContent> templateArgMap = Collections.emptyMap();
		try {

			bizArgs = objectMapper.writeValueAsString(sendWxmpMsgArgs.getParams());

			if (sendWxmpMsgArgs.getWxmpTemplateMsg() == null) {
				reason = String.format("消息模板不存在： AppId: %s BizId: %s", sendWxmpMsgArgs.getAppId(), sendWxmpMsgArgs.getBizId());
				return;
			}
			if (sendWxmpMsgArgs.getOpenId() == null) {
				log.info("openId为空, 忽略此次请求");
				reason = "openId为空，忽略此次请求";
				return;
			}
			if (sendWxmpMsgArgs.getSnsProviderId() == null) {
				log.info("公众号管理Id为空, 忽略此次请求");
				reason = "公众号管理Id为空，忽略此次请求";
				return;
			}


			// 模板参数map
			templateArgMap = Optional.ofNullable(sendWxmpMsgArgs.getWxmpTemplateMsg().getArgs()).orElse(Collections.emptyList())
				.stream()
				.map(templateArg -> {
					SendWxmpMsgByArgs.MessageContent templateArgValue = sendWxmpMsgArgs.getParams().getOrDefault(templateArg.getArgCode(), SendWxmpMsgByArgs.MessageContent.builder().content("").color("").build());
					return Tuple.tuple(templateArg.getTemplateArgCode(), templateArgValue);
				})
				.collect(Collectors.toMap(Tuple2::getV1, Tuple2::getV2, (x1, x2) -> x1));
			// 模板参数
			providerArgsStr = objectMapper.writeValueAsString(templateArgMap);


			List<WxMpTemplateData> data = sendWxmpMsgArgs.getWxmpTemplateMsg().getArgs().stream().map(x -> {
				SendWxmpMsgByArgs.MessageContent messageContent = sendWxmpMsgArgs.getParams().getOrDefault(x.getArgCode(), SendWxmpMsgByArgs.MessageContent.builder().build());
				return new WxMpTemplateData(x.getTemplateArgCode(), substring(Optional.ofNullable(messageContent.getContent()).orElse("")), substring(Optional.ofNullable(messageContent.getColor()).orElse(x.getDefaultColor())));
			}).collect(Collectors.toList());

			providerMsgId = wxMpService.switchoverTo(sendWxmpMsgArgs.getSnsProviderId()).getTemplateMsgService().sendTemplateMsg(WxMpTemplateMessage.builder()
				.templateId(sendWxmpMsgArgs.getWxmpTemplateMsg().getTemplateCode())
				.toUser(sendWxmpMsgArgs.getOpenId())
				.data(data)
				.url(sendWxmpMsgArgs.getJumpUrl() == null ? sendWxmpMsgArgs.getWxmpTemplateMsg().getJumpUrl() : sendWxmpMsgArgs.getJumpUrl())
				.build());
			log.info("wx sendMsg success msgId: {}", providerMsgId);


		} catch (Exception e) {
			log.warn("发送微信模板消息失败", e);
			reason = e.getMessage();

		} finally {
			AtomicReference<String> text = new AtomicReference<>(Optional.ofNullable(sendWxmpMsgArgs.getWxmpTemplateMsg()).map(WxmpTemplateMsg::getTemplateText).orElse(""));
			if (!text.get().isEmpty()) {
				if (sendWxmpMsgArgs.getParams() != null) {
					sendWxmpMsgArgs.getParams().forEach((key, value) -> text.set(text.get().replace("${" + key + "}",
						Optional.ofNullable(value).map(SendWxmpMsgByArgs.MessageContent::getContent).orElse(""))));
				}
			}

			Criteria smsMsgCriteria = Criteria.where(WxmpTemplateMsgRecordMongodb.FIELD.MSG_ID).is(sendWxmpMsgArgs.getMsgId());
			Query smsMsgQuery = Query.query(smsMsgCriteria);
			boolean exists = mongoTemplate.exists(smsMsgQuery, WxmpTemplateMsgRecordMongodb.class, MongodbConstants.Collection.WXMP_TEMPLATE_MSG_RECORD);
			if (!exists) {
				WxmpTemplateMsgRecordMongodb insert = WxmpTemplateMsgRecordMongodb.builder()
					.msgId(sendWxmpMsgArgs.getMsgId())
					.wxmpProviderId(sendWxmpMsgArgs.getSnsProviderId())
					.time(LocalDateTime.now())
					.appId(sendWxmpMsgArgs.getAppId())
					.bizId(sendWxmpMsgArgs.getWxmpTemplateMsg().getBizId())
					.openId(sendWxmpMsgArgs.getOpenId())
					.text(text.get())
					.bizArgs(bizArgs)
					.jumpUrl(sendWxmpMsgArgs.getJumpUrl() == null ? sendWxmpMsgArgs.getWxmpTemplateMsg().getJumpUrl() : sendWxmpMsgArgs.getJumpUrl())
					.source(sendWxmpMsgArgs.getSource())
					.providerType(SnsType.WX_MP.getTypeValue())
					.providerMsgId(providerMsgId)
					.providerTemplateCode(sendWxmpMsgArgs.getWxmpTemplateMsg().getTemplateCode())
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
	}

	/**
	 * 发送微信模板消息保留指定长度
	 *
	 * @param str str
	 * @return String
	 */
	public static String substring(String str) {
		String result = str;
		if (str != null && str.length() > 20) {
			result = str.substring(0, 20);
		}
		return result;

	}
}
