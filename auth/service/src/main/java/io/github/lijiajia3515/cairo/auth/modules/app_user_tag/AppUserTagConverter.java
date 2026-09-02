package io.github.lijiajia3515.cairo.auth.modules.app_user_tag;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_tag.AppUserMetadataTag;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_tag.AppUserTag;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserTagMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;

import java.util.Map;

public class AppUserTagConverter {

	public static AppUserTag convertAppUserTag(AppUserTagMongodb userTagMongodb) {
		return AppUserTag.builder()
			.tagId(userTagMongodb.getTagId())
			.tagName(userTagMongodb.getTagName())
			.enabled(userTagMongodb.getEnabled())
			.build();
	}


	public static AppUserMetadataTag convertAppUserMetadataTag(AppUserTagMongodb m, Map<String, Integer> userCountMap, Map<String, AppUser> metadataUserMap) {
		return AppUserMetadataTag.builder()
			.tagId(m.getTagId())
			.tagName(m.getTagName())
			.enabled(m.getEnabled())
			.userCount(userCountMap.getOrDefault(m.getTagId(), 0))
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(),metadataUserMap))
			.build();
	}
}
