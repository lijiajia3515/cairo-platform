package io.github.lijiajia3515.cairo.auth.modules.notify.category;


import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.category.MetadataNotifyCategory;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.category.NotifyCategory;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.notify.NotifyCategoryMongodb;

import java.util.Map;

public class NotifyCategoryConverter {


	public static MetadataNotifyCategory convertMetadataNotifyCategory(NotifyCategoryMongodb mongo, Map<String, AppUser> metadataUserMap) {

		return MetadataNotifyCategory.builder()
			.categoryId(mongo.getCategoryId())
			.categoryName(mongo.getCategoryName())
			.categoryIcon(mongo.getCategoryIcon())
			.enabled(mongo.isEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(mongo.getMetadata(), metadataUserMap))
			.build();
	}

	public static NotifyCategory convertNotifyCategory(NotifyCategoryMongodb mongo) {

		return NotifyCategory.builder()
			.categoryId(mongo.getCategoryId())
			.categoryName(mongo.getCategoryName())
			.categoryIcon(mongo.getCategoryIcon())
			.enabled(mongo.isEnabled())
			.build();
	}
}
