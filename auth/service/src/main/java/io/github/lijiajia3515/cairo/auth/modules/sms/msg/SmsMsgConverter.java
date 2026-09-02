package io.github.lijiajia3515.cairo.auth.modules.sms.message;


import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.message.MetadataSmsMsg;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsMsgMongodb;

import java.util.Map;
import java.util.Optional;

public class SmsMsgConverter {

	public static MetadataSmsMsg convertMetadataSmsMsg(SmsMsgMongodb mongo, Map<String, App> appMap, Map<String, AppUser> metadataUserMap) {
		return MetadataSmsMsg.builder()
			.msgId(mongo.getMsgId())
			.time(mongo.getTime())
			.appId(mongo.getAppId())
			.appName(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getAppName).orElse(mongo.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getIcon).orElse(null))
			.bizId(mongo.getBizId())
			.phoneNumber(mongo.getPhoneNumber())
			.text(mongo.getText())
			.bizArgs(mongo.getBizArgs())
			.providerType(mongo.getProviderType())
			.providerTemplateCode(mongo.getProviderTemplateCode())
			.providerSign(mongo.getProviderSign())
			.providerArgs(mongo.getProviderArgs())
			.providerMsgId(mongo.getProviderMsgId())
			.success(mongo.isSuccess())
			.reason(mongo.getReason())
			.version(mongo.getVersion())
			.metadata(CairoAppUserConverter.convertAppUser(mongo.getMetadata(), metadataUserMap))
			.build();
	}

}
