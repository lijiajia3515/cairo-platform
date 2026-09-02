package io.github.lijiajia3515.cairo.auth.modules.app_user;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CairoAppUserTool {

	public static Set<String> getAppUserMetadataUserIds(Collection<AppUserMetadataMongodb> metadata) {
		return metadata.stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	public static Stream<String> getMetadataUserIdStream(AppUserMetadataMongodb x) {
		return Stream.of(x.getCreateUserId(), x.getUpdateUserId());
	}
}
