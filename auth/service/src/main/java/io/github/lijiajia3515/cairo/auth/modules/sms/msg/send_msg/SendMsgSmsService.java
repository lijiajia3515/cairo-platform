package io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg;

import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsMsgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.MessageProviderType;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.SendMsgType;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.SmsTemplate;
import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SendMsgSmsService {
	private final AsyncClient asyncClient;
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	public SendMsgSmsService(AsyncClient asyncClient, ObjectMapper objectMapper, MongoTemplate mongoTemplate) {
		this.asyncClient = asyncClient;
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * 根据手机号发送消息
	 */
	@Async
	@SneakyThrows
	public void sendMsg(SendMsgArgs args) {
		SmsTemplate smsTemplate = args.getSmsTemplate();
		Map<String, String> templateArgMap = Collections.emptyMap();
		String bizArgsStr = null;
		String providerSign = null;
		String providerArgsStr = null;
		String providerMsgId = null;
		boolean success = false;
		String reason = null;
		try {
			bizArgsStr = objectMapper.writeValueAsString(args.getBizArgs());
			// 短信签名
			providerSign = Optional.ofNullable(args.getBizSign())
				.or(() -> Optional.ofNullable(smsTemplate).map(SmsTemplate::getTemplateSign))
				.orElse(null);

			if (smsTemplate == null) {
				reason = String.format("消息模板不存在： AppId: %s BizId: %s", args.getAppId(), args.getBizId());
				return;
			}
			if (args.getPhoneNumber() == null) {
				log.debug("手机号为空, 忽略此次请求");
				reason = "手机号为空，忽略此次请求";
				return;
			}

			// 模板参数map
			templateArgMap = Optional.ofNullable(smsTemplate.getArgs()).orElse(Collections.emptyList())
				.stream()
				.map(templateArg -> {
					String templateArgValue = args.getBizArgs().getOrDefault(templateArg.getArgCode(), "");
					return Tuple.tuple(templateArg.getTemplateArgCode(), templateArgValue);
				})
				.collect(Collectors.toMap(Tuple2::getV1, Tuple2::getV2, (x1, x2) -> x1));

			// 模板参数
			providerArgsStr = objectMapper.writeValueAsString(templateArgMap);

			SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
				.signName(providerSign)
				.templateCode(smsTemplate.getTemplateCode())
				.phoneNumbers(args.getPhoneNumber())
				.templateParam(providerArgsStr)
				.build();

			CompletableFuture<SendSmsResponse> response = asyncClient.sendSms(sendSmsRequest);
			SendSmsResponse sendSmsResponse = response.get();
			providerMsgId = Optional.ofNullable(sendSmsResponse).map(SendSmsResponse::getBody).map(SendSmsResponseBody::getBizId).orElse(null);
			String providerResponseCode = Optional.ofNullable(sendSmsResponse).map(SendSmsResponse::getBody).map(SendSmsResponseBody::getCode).orElse("");
			String providerResponseMessage = Optional.ofNullable(sendSmsResponse).map(SendSmsResponse::getBody).map(SendSmsResponseBody::getMessage).orElse("");
			if (providerResponseCode.equals("OK")) {
				success = true;
			} else {
				reason = String.format(
					"Code: %s, Message: %s", providerResponseCode, providerResponseMessage
				);
			}
		} catch (Exception e) {
			log.warn("send message by phone number: ", e);
			reason = e.getMessage();
		} finally {
			AtomicReference<String> text = new AtomicReference<>(Optional.ofNullable(smsTemplate).map(SmsTemplate::getTemplateText).orElse(""));
			if (!text.get().isEmpty()) {
				templateArgMap.forEach((key, value) -> {
					text.set(text.get().replace("${" + key + "}", value));
				});
			}

			Criteria smsMsgCriteria = Criteria.where(SmsMsgMongodb.FIELD.MSG_ID).is(args.getMsgId());
			Query smsMsgQuery = Query.query(smsMsgCriteria);
			boolean exists = mongoTemplate.exists(smsMsgQuery, SmsMsgMongodb.class, MongodbConstants.Collection.SMS_MSG);
			if (!exists) {
				mongoTemplate.insert(buildSmsMsgPhoneNumber(
					args.getMsgId(),
					args.getTime(),
					args.getAppId(),
					args.getPhoneNumber(),
					text.get(),
					args.getBizId(),
					bizArgsStr,
					providerSign,
					Optional.ofNullable(smsTemplate).map(SmsTemplate::getTemplateCode).orElse(null),
					providerArgsStr,
					providerMsgId,
					success,
					reason
				), MongodbConstants.Collection.SMS_MSG);
			} else {
				// appId,bizId, 不变更
				Update update = Update.update(SmsMsgMongodb.FIELD.TIME, args.getTime());
				update.set(SmsMsgMongodb.FIELD.PHONE_NUMBER, args.getPhoneNumber());
				update.set(SmsMsgMongodb.FIELD.BIZ_ARGS, bizArgsStr);
				update.set(SmsMsgMongodb.FIELD.TEXT, text.get());
				update.set(SmsMsgMongodb.FIELD.PROVIDER_SIGN, providerSign);
				update.set(SmsMsgMongodb.FIELD.PROVIDER_ARGS, providerArgsStr);
				update.set(SmsMsgMongodb.FIELD.PROVIDER_BIZ_ID, providerMsgId);
				update.set(SmsMsgMongodb.FIELD.SUCCESS, success);
				update.set(SmsMsgMongodb.FIELD.REASON, reason);
				update.currentDate(SmsMsgMongodb.FIELD.METADATA.UPDATE_TIME);
				update.inc(SmsMsgMongodb.FIELD.VERSION);
				mongoTemplate.updateFirst(smsMsgQuery, update, SmsMsgMongodb.class, MongodbConstants.Collection.SMS_MSG);
			}
		}
	}

	@Async
	public void sendBatchMessage(List<SendMsgArgs> argsList) {
		for (SendMsgArgs args : argsList) {
			sendMsg(args);
		}
	}

	/**
	 * @param msgId         唯一消息ID
	 * @param appId             应用ID
	 * @param phoneNumber       手机号
	 * @param text              短信文本
	 * @param bizId             业务ID
	 * @param bizArgs           业务参数
	 * @param sign              签名
	 * @param templateCode      模板编码
	 * @param providerArgs      云参数参数
	 * @param providerMsgId 供应商消息ID（唯一）
	 * @param success           结果
	 * @param reason            原因
	 * @return sms message mongodb
	 */
	public SmsMsgMongodb buildSmsMsgPhoneNumber(String msgId, LocalDateTime time, String appId, String phoneNumber, String text, String bizId, String bizArgs, String sign, String templateCode, String providerArgs, String providerMsgId, boolean success, String reason) {
		return SmsMsgMongodb.builder()
			.msgId(msgId)
			.time(time)
			.appId(appId)
			.bizId(bizId)
			.type(SendMsgType.PHONE_NUMBER.name())
			.phoneNumber(phoneNumber)
			.text(text)
			.bizArgs(bizArgs)
			.providerType(MessageProviderType.ALIYUN.name())
			.providerSign(sign)
			.providerTemplateCode(templateCode)
			.providerArgs(providerArgs)
			.providerMsgId(providerMsgId)
			.success(success)
			.reason(reason)
			.metadata(AppUserMetadataMongodb.builder().build())
			.build();
	}
}
