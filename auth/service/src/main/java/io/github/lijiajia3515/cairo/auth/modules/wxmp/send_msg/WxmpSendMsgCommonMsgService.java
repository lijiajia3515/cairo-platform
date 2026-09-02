package io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg;


import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.args.SendWxmpMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg.WxmpTemplateMsgCommonService;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;


/**
 * [client/api] wxmp service
 * 微信模板消息
 */
@Slf4j
@Validated
@Component
public class WxmpSendMsgCommonMsgService {

	private final AsyncSendWxmpMsgService asyncSendWxmpMsgService;
	private final WxmpTemplateMsgCommonService wxmpTemplateMsgCommonService;
	private final MongoTemplate readMongoTemplate;

	public WxmpSendMsgCommonMsgService(AsyncSendWxmpMsgService asyncSendWxmpMsgService,
										   WxmpTemplateMsgCommonService wxmpTemplateMsgCommonService,
										   MongoTemplate readMongoTemplate
	) {
		this.asyncSendWxmpMsgService = asyncSendWxmpMsgService;
		this.wxmpTemplateMsgCommonService = wxmpTemplateMsgCommonService;
		this.readMongoTemplate = readMongoTemplate;
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
	public void sendWxmpMsgMsgByAppUser(String appId, SendWxmpMsgByArgs args) {
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

				asyncSendWxmpMsgService.send(SendWxmpMsgArgs.builder()
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
}
