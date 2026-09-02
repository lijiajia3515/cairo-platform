package io.github.lijiajia3515.cairo.auth.modules.subapp;

import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.MetadataSubapp;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;

import java.util.Map;
import java.util.Optional;

/**
 * subapp converter
 */
public class SubappConverter {

	public static MetadataSubapp convertMetadataSubapp(SubappMongodb m, Map<String, App> appMap, Map<String, Endpoint> endpointMap, Map<String, AppUser> metadataUserMap) {
		return MetadataSubapp.builder()
			.id(m.getId())
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.subappId(m.getSubappId())
			.subappName(m.getSubappName())
			.subappIcon(m.getSubappIcon())
			.scope(m.getScope())
			.enabled(m.getEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static Subapp convertSubapp(SubappMongodb m, Map<String, App> appMap, Map<String, Endpoint> endpointMap) {
		return Subapp.builder()
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.subappId(m.getSubappId())
			.subappName(m.getSubappName())
			.subappIcon(m.getSubappIcon())
			.scope(m.getScope())
			.enabled(m.getEnabled())
			.build();
	}

	public static Subapp convertBasicSubapp(SubappMongodb m) {
		return Subapp.builder()
			.appId(m.getAppId())
			.endpointId(m.getEndpointId())
			.subappId(m.getSubappId())
			.subappName(m.getSubappName())
			.subappIcon(m.getSubappIcon())
			.scope(m.getScope())
			.enabled(m.getEnabled())
			.build();
	}


}
