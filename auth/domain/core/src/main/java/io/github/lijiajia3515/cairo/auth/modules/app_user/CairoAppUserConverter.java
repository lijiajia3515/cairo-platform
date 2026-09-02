package io.github.lijiajia3515.cairo.auth.modules.app_user;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;

import java.util.Map;
import java.util.Optional;

public class CairoAppUserConverter {

	public static CairoAppUserMetadata convertAppUser(AppUserMetadataMongodb mongodb, Map<String, AppUser> metadataUserMap) {
		return CairoAppUserMetadata.builder()
			.createUser(Optional.ofNullable(metadataUserMap.get(mongodb.getCreateUserId())).orElse(AppUser.builder()
				.userId(mongodb.getCreateUserId())
				.nickname(mongodb.getCreateUserId())
				.build())
			)
			.updateUser(Optional.ofNullable(metadataUserMap.get(mongodb.getUpdateUserId())).orElse(AppUser.builder()
				.userId(mongodb.getUpdateUserId())
				.nickname(mongodb.getUpdateUserId())
				.build())
			)
			.createTime(mongodb.getCreateTime())
			.updateTime(mongodb.getUpdateTime())
			.build();
	}
}
