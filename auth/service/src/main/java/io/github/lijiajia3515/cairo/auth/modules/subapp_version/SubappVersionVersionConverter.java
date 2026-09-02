package io.github.lijiajia3515.cairo.auth.modules.subapp_version;

import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.MetadataSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.SubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappVersionMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;

import java.util.Map;
import java.util.Optional;

/**
 * subappVersion converter
 */
public class SubappVersionVersionConverter {

	public static MetadataSubappVersion convertMetadataSubapp(SubappVersionMongodb m, Map<String, Subapp> subappMap, Map<String, AppUser> metadataUserMap) {
		return MetadataSubappVersion.builder()
			.subappId(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappId).orElse(m.getSubappId()))
			.subappName(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappName).orElse(null))
			.subappVersion(m.getSubappVersion())
			.subappRemark(m.getSubappRemark())
			.enabled(m.getEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static SubappVersion convertSubappVersion(SubappVersionMongodb m, Map<String, Subapp> subappMap) {
		return SubappVersion.builder()
			.subappId(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappId).orElse(m.getSubappId()))
			.subappName(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappName).orElse(null))
			.subappVersion(m.getSubappVersion())
			.subappRemark(m.getSubappRemark())
			.enabled(m.getEnabled())
			.build();
	}

	public static SubappVersion convertBasicSubapp(SubappVersionMongodb m) {
		return SubappVersion.builder()
			.subappId(m.getSubappId())
			.subappVersion(m.getSubappVersion())
			.subappRemark(m.getSubappRemark())
			.enabled(m.getEnabled())
			.build();
	}


}
