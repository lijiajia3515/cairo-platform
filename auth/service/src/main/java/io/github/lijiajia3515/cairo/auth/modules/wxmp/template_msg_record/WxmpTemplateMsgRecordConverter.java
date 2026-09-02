package io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg_record;

import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg_record.MetadataWxmpTemplateMsgRecord;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgRecordMongodb;

import java.util.Map;
import java.util.Optional;

public class WxmpTemplateMsgRecordConverter {

	public static MetadataWxmpTemplateMsgRecord convertMetadataWxmpTemplateMsgRecord(WxmpTemplateMsgRecordMongodb mongo, Map<String, App> appMap, Map<String, AppUser> metadataUserMap) {
		return MetadataWxmpTemplateMsgRecord.builder()
			.msgId(mongo.getMsgId())
			.wxmpProviderId(mongo.getWxmpProviderId())
			.time(mongo.getTime())
			.appId(mongo.getAppId())
			.appName(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getAppName).orElse(mongo.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getIcon).orElse(null))
			.bizId(mongo.getBizId())
			.openId(mongo.getOpenId())
			.text(mongo.getText())
			.bizArgs(mongo.getBizArgs())
			.providerType(mongo.getProviderType())
			.providerTemplateCode(mongo.getProviderTemplateCode())
			.providerArgs(mongo.getProviderArgs())
			.providerMsgId(mongo.getProviderMsgId())
			.success(mongo.isSuccess())
			.reason(mongo.getReason())
			.version(mongo.getVersion())
			.jumpUrl(mongo.getJumpUrl())
			.source(mongo.getSource())
			.metadata(CairoAppUserConverter.convertAppUser(mongo.getMetadata(), metadataUserMap))
			.build();
	}

}
