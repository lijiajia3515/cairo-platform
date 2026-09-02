package io.github.lijiajia3515.cairo.auth.modules.sms.template;


import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.MetadataSmsTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.SmsTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.SmsTemplateArg;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsTemplateArgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsTemplateMongodb;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SmsTemplateConverter {

	public static SmsTemplate convertSmsTemplate(SmsTemplateMongodb mongo, Map<String, App> appMap, Map<String, List<SmsTemplateArgMongodb>> argMap) {
		return SmsTemplate.builder()
			.appId(mongo.getAppId())
			.appName(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getAppName).orElse(mongo.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getIcon).orElse(null))
			.bizId(mongo.getBizId())
			.templateName(mongo.getTemplateName())
			.templateSign(mongo.getTemplateSign())
			.templateCode(mongo.getTemplateCode())
			.templateType(mongo.getTemplateType())
			.templateText(mongo.getTemplateText())
			.args(argMap.getOrDefault(mongo.getBizId(), Collections.emptyList()).stream().map(SmsTemplateConverter::convertSmsArg).collect(Collectors.toList()))
			.enabled(mongo.isEnabled())
			.build();
	}

	public static MetadataSmsTemplate convertMetadataSmsTemplate(SmsTemplateMongodb mongo, Map<String, App> appMap, Map<String, List<SmsTemplateArgMongodb>> argMap, Map<String, AppUser> metadataUserMap) {
		return MetadataSmsTemplate.builder()
			.appId(mongo.getAppId())
			.appName(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getAppName).orElse(mongo.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(mongo.getAppId())).map(App::getIcon).orElse(null))
			.bizId(mongo.getBizId())
			.templateName(mongo.getTemplateName())
			.templateSign(mongo.getTemplateSign())
			.templateCode(mongo.getTemplateCode())
			.templateType(mongo.getTemplateType())
			.templateText(mongo.getTemplateText())
			.args(argMap.getOrDefault(mongo.getBizId(), Collections.emptyList()).stream().map(SmsTemplateConverter::convertSmsArg).collect(Collectors.toList()))
			.enabled(mongo.isEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(mongo.getMetadata(), metadataUserMap))
			.build();
	}

	public static SmsTemplateArg convertSmsArg(SmsTemplateArgMongodb mongodb) {
		return SmsTemplateArg.builder()
			.argCode(mongodb.getArgCode())
			.argName(mongodb.getArgName())
			.argType(mongodb.getArgType())
			.templateArgCode(mongodb.getTemplateArgCode())
			.build();
	}

}
