package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.CairoTenantAppUserMetadata;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;

import java.util.Map;
import java.util.Optional;

public class CairoTenantAppUserConverter {
	public static CairoTenantAppUserMetadata convertTenantAppUser(TenantAppUserMetadataMongodb mongodb, Map<String, TenantAppUser> metadataUserMap) {
		return CairoTenantAppUserMetadata.builder()
			.createUser(Optional.ofNullable(metadataUserMap.get(mongodb.getCreateUserId())).orElse(TenantAppUser.builder()
				.userId(mongodb.getCreateUserId())
				.nickname(mongodb.getCreateUserId())
				.build())
			)
			.updateUser(Optional.ofNullable(metadataUserMap.get(mongodb.getUpdateUserId())).orElse(TenantAppUser.builder()
				.userId(mongodb.getUpdateUserId())
				.nickname(mongodb.getUpdateUserId())
				.build())
			)
			.createTime(mongodb.getCreateTime())
			.updateTime(mongodb.getUpdateTime())
			.build();
	}
}
