package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CairoTenantAppUserTool {

	public static Set<String> getTenantAppUserMetadataUserIds(Collection<TenantAppUserMetadataMongodb> metadata) {
		return metadata.stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	public static Stream<String> getTenantMetadataUserIdStream(TenantAppUserMetadataMongodb x) {
		return Stream.of(x.getCreateUserId(), x.getUpdateUserId());
	}
}
