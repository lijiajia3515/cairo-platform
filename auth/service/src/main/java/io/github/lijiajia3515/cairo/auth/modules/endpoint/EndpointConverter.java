package io.github.lijiajia3515.cairo.auth.modules.endpoint;

import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.MetadataEndpoint;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;

import java.util.Map;
import java.util.Optional;

/**
 * app endpoint converter
 */
public class EndpointConverter {

	public static MetadataEndpoint convertMetadataEndpoint(EndpointMongodb m, Map<String, App> appMap, Map<String, AppUser> metadataUserMap) {
		return MetadataEndpoint.builder()
			.id(m.getId())
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(m.getEndpointName())
			.icon(m.getIcon())
			.type(m.getType())
			.scope(m.getScope())
			.icon(m.getIcon())
			.websiteUrl(m.getWebsiteUrl())
			.enabled(m.getEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static Endpoint convertEndpoint(EndpointMongodb m, Map<String, App> appMap) {
		return Endpoint.builder()
			.id(m.getId())
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(m.getEndpointName())
			.icon(m.getIcon())
			.type(m.getType())
			.scope(m.getScope())
			.icon(m.getIcon())
			.websiteUrl(m.getWebsiteUrl())
			// .enabled(m.getEnabled())
			.build();
	}

	public static Endpoint convertBasicEndpoint(EndpointMongodb m) {
		return Endpoint.builder()
			.endpointId(m.getEndpointId())
			.endpointName(m.getEndpointName())
			.type(m.getType())
			.scope(m.getScope())
			.icon(m.getIcon())
			.build();
	}


}
