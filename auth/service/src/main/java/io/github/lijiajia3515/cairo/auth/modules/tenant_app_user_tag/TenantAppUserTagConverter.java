package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_tag;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_tag.MetadataTenantAppUserTag;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_tag.TenantAppUserTag;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTagMongodb;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;

import java.util.Map;

public class TenantAppUserTagConverter {
	public static TenantAppUserTag convertTenantAppUserTag(TenantAppUserTagMongodb tenantAppUserTagMongodb) {
		return TenantAppUserTag.builder()
			.tagId(tenantAppUserTagMongodb.getTagId())
			.tagName(tenantAppUserTagMongodb.getTagName())
			.enabled(tenantAppUserTagMongodb.getEnabled())
			.build();
	}

	public static MetadataTenantAppUserTag convertMetadataTenantAppUserTag(TenantAppUserTagMongodb m, Map<String, Integer> userCountMap, Map<String, TenantAppUser> metadataUserMap) {
		return MetadataTenantAppUserTag.builder()
			.tagId(m.getTagId())
			.tagName(m.getTagName())
			.enabled(m.getEnabled())
			.userCount(userCountMap.getOrDefault(m.getTagId(), 0))
			.metadata(CairoTenantAppUserConverter.convertTenantAppUser(m.getMetadata(),metadataUserMap))
			.build();
	}

}
