package io.github.lijiajia3515.cairo.auth.modules.notify.template.common;


import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.notify.NotifyTemplateArgsMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.notify.NotifyTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.category.NotifyCategory;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.template.MetadataNotifyTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.template.NotifyArgsTypes;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.template.NotifyTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.template.NotifyTemplateArgs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class NotifyTemplateConverter {

	public static NotifyTemplate convertNotifyTemplate(NotifyTemplateMongodb mongo, Map<String, NotifyCategory> categoryMap, Map<String, List<NotifyTemplateArgsMongodb>> argMap) {
		return NotifyTemplate.builder()
			.templateId(mongo.getTemplateId())
			.templateName(mongo.getTemplateName())
			.categoryId(mongo.getCategoryId())
			.categoryName(Optional.ofNullable(categoryMap.get(mongo.getCategoryId())).map(NotifyCategory::getCategoryName).orElse(mongo.getCategoryId()))
			.categoryIcon(Optional.ofNullable(categoryMap.get(mongo.getCategoryId())).map(NotifyCategory::getCategoryIcon).orElse(null))
			.messageCode(mongo.getMessageCode())
			.messageTitle(mongo.getMessageTitle())
			.messageIcon(mongo.getMessageIcon())
			.messageAlert(mongo.getMessageAlert())
			.messageType(mongo.getMessageType())
			.messageContent(mongo.getMessageContent())
			.alertArgs(argMap.getOrDefault(NotifyArgsTypes.ALERT, Collections.emptyList()).stream().map(NotifyTemplateConverter::convertArgs).collect(Collectors.toList()))
			.contentArgs(argMap.getOrDefault(NotifyArgsTypes.CONTENT, Collections.emptyList()).stream().map(NotifyTemplateConverter::convertArgs).collect(Collectors.toList()))
			.templateArgs(argMap.getOrDefault(NotifyArgsTypes.TEMPLATE, Collections.emptyList()).stream().map(NotifyTemplateConverter::convertArgs).collect(Collectors.toList()))
			.linkType(mongo.getLinkType())
			.pageUrl(mongo.getPageUrl())
			.linkUrl(mongo.getLinkUrl())
			.enabled(mongo.isEnabled())
			.build();
	}

	public static MetadataNotifyTemplate convertMetadataNotifyTemplate(NotifyTemplateMongodb mongo, Map<String, NotifyCategory> categoryMap, Map<String, List<NotifyTemplateArgsMongodb>> argMap, Map<String, AppUser> metadataUserMap) {
		return MetadataNotifyTemplate.builder()
			.templateId(mongo.getTemplateId())
			.templateName(mongo.getTemplateName())
			.categoryId(mongo.getCategoryId())
			.categoryName(Optional.ofNullable(categoryMap.get(mongo.getCategoryId())).map(NotifyCategory::getCategoryName).orElse(mongo.getCategoryId()))
			.categoryIcon(Optional.ofNullable(categoryMap.get(mongo.getCategoryId())).map(NotifyCategory::getCategoryIcon).orElse(null))
			.messageCode(mongo.getMessageCode())
			.messageTitle(mongo.getMessageTitle())
			.messageIcon(mongo.getMessageIcon())
			.messageAlert(mongo.getMessageAlert())
			.messageType(mongo.getMessageType())
			.messageContent(mongo.getMessageContent())
			.alertArgs(argMap.getOrDefault(mongo.getTemplateId(), Collections.emptyList()).stream().filter(x -> x.getArgsType().equals(NotifyArgsTypes.ALERT)).map(NotifyTemplateConverter::convertArgs).collect(Collectors.toList()))
			.contentArgs(argMap.getOrDefault(mongo.getTemplateId(), Collections.emptyList()).stream().filter(x -> x.getArgsType().equals(NotifyArgsTypes.CONTENT)).map(NotifyTemplateConverter::convertArgs).collect(Collectors.toList()))
			.templateArgs(argMap.getOrDefault(mongo.getTemplateId(), Collections.emptyList()).stream().filter(x -> x.getArgsType().equals(NotifyArgsTypes.TEMPLATE)).map(NotifyTemplateConverter::convertArgs).collect(Collectors.toList()))
			.linkType(mongo.getLinkType())
			.pageUrl(mongo.getPageUrl())
			.linkUrl(mongo.getLinkUrl())
			.enabled(mongo.isEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(mongo.getMetadata(), metadataUserMap))
			.build();
	}

	public static NotifyTemplateArgs convertArgs(NotifyTemplateArgsMongodb mongodb) {
		return NotifyTemplateArgs.builder()
			.argsCode(mongodb.getArgsCode())
			.argsName(mongodb.getArgsName())
			.dataType(mongodb.getDataType())
			.defaultValue(mongodb.getDefaultValue())
			.build();
	}

}
