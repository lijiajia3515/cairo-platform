package io.github.lijiajia3515.cairo.auth.modules.app_release;

import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.OpenAppRelease;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_release.AppRelease;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_release.AppUserMetadataAppRelease;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_release.MetadataAppRelease;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppReleaseMongodb;

import java.util.Map;
import java.util.Optional;

public class AppReleaseConverter {

	public static MetadataAppRelease convertMetadataAppReleaseVersion(AppReleaseMongodb m, Map<String, App> appMap, Map<String, Endpoint> endpoints, Map<String, AppUser> metadataUserMap) {
		return MetadataAppRelease.builder()
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpoints.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpoints.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.typeId(m.getType())
			.typeName(m.getType())
			.title(m.getTitle())
			.webUrl(m.getWebUrl())
			.appVersion(m.getAppVersion())
			.releaseVersion(m.getReleaseVersion())
			.latestVersion(m.getLatestVersion())
			.remark(m.getRemark())
			.force(m.getForce())
			.androidApkUrl(m.getAndroidApkUrl())
			.iosAppStoreUrl(m.getIosAppStoreUrl())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static AppUserMetadataAppRelease convertAppUserMetadataAppReleaseVersion(AppReleaseMongodb m, Map<String, App> appMap, Map<String, Endpoint> endpoints, Map<String, AppUser> metadataUserMap) {
		return AppUserMetadataAppRelease.builder()
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpoints.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpoints.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.typeId(m.getType())
			.typeName(m.getType())
			.title(m.getTitle())
			.webUrl(m.getWebUrl())
			.appVersion(m.getAppVersion())
			.releaseVersion(m.getReleaseVersion())
			.latestVersion(m.getLatestVersion())
			.remark(m.getRemark())
			.force(m.getForce())
			.androidApkUrl(m.getAndroidApkUrl())
			.iosAppStoreUrl(m.getIosAppStoreUrl())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static AppRelease convertAppReleaseVersion(AppReleaseMongodb m, Map<String, App> appMap, Map<String, Endpoint> endpoints) {
		return AppRelease.builder()
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpoints.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpoints.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.typeId(m.getType())
			.typeName(m.getType())
			.title(m.getTitle())
			.webUrl(m.getWebUrl())
			.appVersion(m.getAppVersion())
			.releaseVersion(m.getReleaseVersion())
			.latestVersion(m.getLatestVersion())
			.remark(m.getRemark())
			.force(m.getForce())
			.androidApkUrl(m.getAndroidApkUrl())
			.iosAppStoreUrl(m.getIosAppStoreUrl())
			.updateTime(m.getMetadata().getUpdateTime())
			.build();
	}

	public static OpenAppRelease convertOpenAppReleaseVersion(AppReleaseMongodb m, Map<String, Endpoint> endpointMap) {
		return OpenAppRelease.builder()
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.typeId(m.getType())
			.typeName(m.getType())
			.appVersion(m.getAppVersion())
			.releaseVersion(m.getReleaseVersion())
			.latestVersion(m.getLatestVersion())
			.remark(m.getRemark())
			.force(m.getForce())
			.androidApkUrl(m.getAndroidApkUrl())
			.iosAppStoreUrl(m.getIosAppStoreUrl())
			.webUrl(m.getWebUrl())
			.title(m.getTitle())
			.updateTime(m.getMetadata().getUpdateTime())
			.build();
	}
}
