package io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg;


import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.MetadataWxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsgArg;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgArgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTemplateMsgMongodb;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WxmpTemplateMsgConverter {

	public static WxmpTemplateMsg convertWxmpTemplateMsg(WxmpTemplateMsgMongodb mongo, Map<String, App> appMap, Map<String, List<WxmpTemplateMsgArgMongodb>> argMap) {
		return WxmpTemplateMsg.builder()
			.appId(mongo.getAppId())
			.appName(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getAppName).orElse(mongo.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getIcon).orElse(null))
			.bizId(mongo.getBizId())
			.templateName(mongo.getTemplateName())
			.templateCode(mongo.getTemplateCode())
			.templateType(mongo.getTemplateType())
			.templateText(mongo.getTemplateText())
			.args(argMap.getOrDefault(mongo.getBizId(), Collections.emptyList()).stream().map(WxmpTemplateMsgConverter::convertWxmsArg).collect(Collectors.toList()))
			.enabled(mongo.isEnabled())
			.wxmpProviderId(mongo.getWxmpProviderId())
			.jumpUrl(mongo.getJumpUrl())
			.build();
	}

	public static MetadataWxmpTemplateMsg convertMetadataWxmpTemplateMsg(WxmpTemplateMsgMongodb mongo, Map<String, App> appMap, Map<String, List<WxmpTemplateMsgArgMongodb>> argMap, Map<String, AppUser> metadataUserMap) {
		return MetadataWxmpTemplateMsg.builder()
			.appId(mongo.getAppId())
			.appName(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getAppName).orElse(mongo.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getIcon).orElse(null))
			.bizId(mongo.getBizId())
			.templateName(mongo.getTemplateName())
			.templateCode(mongo.getTemplateCode())
			.templateType(mongo.getTemplateType())
			.templateText(mongo.getTemplateText())
			.args(argMap.getOrDefault(mongo.getBizId(), Collections.emptyList()).stream().map(WxmpTemplateMsgConverter::convertWxmsArg).collect(Collectors.toList()))
			.enabled(mongo.isEnabled())
			.jumpUrl(mongo.getJumpUrl())
			.wxmpProviderId(mongo.getWxmpProviderId())
			.metadata(CairoAppUserConverter.convertAppUser(mongo.getMetadata(), metadataUserMap))
			.build();
	}

	public static WxmpTemplateMsgArg convertWxmsArg(WxmpTemplateMsgArgMongodb mongodb) {
		return WxmpTemplateMsgArg.builder()
			.argCode(mongodb.getArgCode())
			.argName(mongodb.getArgName())
			.argType(mongodb.getArgType())
			.defaultColor(mongodb.getDefaultColor())
			.templateArgCode(mongodb.getTemplateArgCode())
			.build();
	}

}
