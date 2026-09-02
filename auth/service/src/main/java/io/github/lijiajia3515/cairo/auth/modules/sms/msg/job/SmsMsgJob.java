package io.github.lijiajia3515.cairo.auth.modules.sms.message.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsMsgMongodb;
import io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.biz_log.tenant_app_biz_log.TenantAppBizLogCairoWebManageApiController;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.common.args.SendPhoneNumberSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.common.SmsMsgCommonService;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg.SendMsgSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短息消息任务类
 */
@Slf4j
@Component
public class SmsMsgJob {
	private final MongoTemplate readMongoTemplate;
	private final SmsMsgCommonService smsMsgCommonService;
	private final ObjectMapper objectMapper;
	private final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
		@Override
		public Type getType() {
			return super.getType();
		}
	};

	public SmsMsgJob(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
						 SmsMsgCommonService smsMsgCommonService, SendMsgSmsService sendMsgSmsService, ObjectMapper objectMapper, TenantAppBizLogCairoWebManageApiController tenantAppBizLogCairoWebManageApiController) {
		this.smsMsgCommonService = smsMsgCommonService;
		this.readMongoTemplate = readMongoTemplate;
		this.objectMapper = objectMapper;
	}


	/**
	 * 重试失败短信消息任务
	 */
	@XxlJob("retryFailedSmsMsgJob")
	public void retryFailedSmsMsgJob() {
		XxlJobHelper.log("job start");
		try {
			//查询短信记录为失败的，且失败次数小于3次的。
			Criteria criteria = Criteria
				.where(SmsMsgMongodb.FIELD.SUCCESS).is(false)
				.and(SmsMsgMongodb.FIELD.VERSION).lt(3)
				.and(SmsMsgMongodb.FIELD.PHONE_NUMBER).ne(null);
			Query query = Query.query(criteria);
			List<SmsMsgMongodb> failedMessageList = readMongoTemplate.find(query, SmsMsgMongodb.class, MongodbConstants.Collection.SMS_MSG);
			XxlJobHelper.log("需要重试的短信数量： {}", failedMessageList.size());
			List<SendPhoneNumberSmsMsgArgs> messageList = failedMessageList.stream()

				.map(failedMessage -> {
					Map<String, String> args = Collections.emptyMap();
					try {
						objectMapper.readValue(failedMessage.getBizArgs(), MAP_TYPE);
					} catch (JsonProcessingException e) {
						log.debug("parse map type error", e);
					}
					return SendPhoneNumberSmsMsgArgs.builder()
						.msgId(failedMessage.getMsgId())
						.time(failedMessage.getTime())
						.phoneNumber(failedMessage.getPhoneNumber())
						.appId(failedMessage.getAppId())
						.bizId(failedMessage.getBizId())
						.sign(failedMessage.getProviderSign())
						.args(args)
						.build();
				}).collect(Collectors.toList());
			messageList.forEach(smsMsgCommonService::sendMsgByPhoneNumber);

		} catch (Exception e) {
			log.error("error{}", e.getMessage());
			XxlJobHelper.log(e.getMessage());
			XxlJobHelper.handleFail("job fail");
			throw new RuntimeException(e);
		}
		XxlJobHelper.log("job end");
	}
}
