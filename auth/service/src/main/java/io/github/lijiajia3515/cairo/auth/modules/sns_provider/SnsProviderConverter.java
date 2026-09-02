package io.github.lijiajia3515.cairo.auth.modules.sns_provider;

import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.BasicSnsProvider;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.MetadataSnsProvider;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.SnsProvider;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsProviderMongodb;
import io.github.lijiajia3515.cairo.auth.framework.sns.ProviderPartnerProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.ProviderTypeProperties;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;

import java.util.Map;
import java.util.Optional;

public class SnsProviderConverter {

	public static SnsProvider convertSnsProvider(SnsProviderMongodb snsProviderMongodb,
												 Map<String, App> appMap,
												 Map<String, ProviderTypeProperties> providerTypeMap,
												 Map<String, ProviderPartnerProperties> providerPartnerMap) {
		return SnsProvider.builder()
			.appId(snsProviderMongodb.getAppId())
			.appName(Optional.ofNullable(appMap.get(snsProviderMongodb.getAppId())).map(App::getAppName).orElse(snsProviderMongodb.getAppId()))
			.appName(Optional.ofNullable(appMap.get(snsProviderMongodb.getAppId())).map(App::getIcon).orElse(null))
			.snsProviderId(snsProviderMongodb.getSnsProviderId())
			.snsProviderName(snsProviderMongodb.getSnsProviderName())
			.snsProviderTypeId(snsProviderMongodb.getSnsProviderType())
			.snsProviderTypeName(Optional.ofNullable(providerTypeMap.get(snsProviderMongodb.getSnsProviderType())).map(ProviderTypeProperties::getName).orElse(snsProviderMongodb.getSnsProviderType()))
			.snsProviderPartnerId(snsProviderMongodb.getSnsProviderPartner())
			.snsProviderPartnerName(Optional.ofNullable(providerPartnerMap.get(snsProviderMongodb.getSnsProviderPartner())).map(ProviderPartnerProperties::getName).orElse(snsProviderMongodb.getSnsProviderPartner()))
			.snsProviderPartnerIcon(Optional.ofNullable(providerPartnerMap.get(snsProviderMongodb.getSnsProviderPartner())).map(ProviderPartnerProperties::getIcon).orElse(null))
			.clientId(snsProviderMongodb.getClientId())
			//.clientSecret(snsProviderMongodb.getClientSecret())
			.enabled(snsProviderMongodb.getEnabled())
			.isAutoRegister(snsProviderMongodb.getIsAutoRegister())
			.build();
	}

	public static MetadataSnsProvider convertMetadataSnsProvider(SnsProviderMongodb snsProviderMongodb,
																 Map<String, App> appMap,
																 Map<String, ProviderTypeProperties> providerTypeMap,
																 Map<String, ProviderPartnerProperties> providerPartnerMap,
																 Map<String, AppUser> metadataUserMap ) {
		return MetadataSnsProvider.builder()
			.appId(snsProviderMongodb.getAppId())
			.appName(Optional.ofNullable(appMap.get(snsProviderMongodb.getAppId())).map(App::getAppName).orElse(snsProviderMongodb.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(snsProviderMongodb.getAppId())).map(App::getIcon).orElse(null))
			.snsProviderId(snsProviderMongodb.getSnsProviderId())
			.snsProviderName(snsProviderMongodb.getSnsProviderName())
			.snsProviderTypeId(snsProviderMongodb.getSnsProviderType())
			.snsProviderTypeName(Optional.ofNullable(providerTypeMap.get(snsProviderMongodb.getSnsProviderType())).map(ProviderTypeProperties::getName).orElse(snsProviderMongodb.getSnsProviderType()))
			.snsProviderPartnerId(snsProviderMongodb.getSnsProviderPartner())
			.snsProviderPartnerName(Optional.ofNullable(providerPartnerMap.get(snsProviderMongodb.getSnsProviderPartner())).map(ProviderPartnerProperties::getName).orElse(snsProviderMongodb.getSnsProviderPartner()))
			.snsProviderPartnerIcon(Optional.ofNullable(providerPartnerMap.get(snsProviderMongodb.getSnsProviderPartner())).map(ProviderPartnerProperties::getIcon).orElse(null))
			.clientId(snsProviderMongodb.getClientId())
			.clientSecret(snsProviderMongodb.getClientSecret())
			.enabled(snsProviderMongodb.getEnabled())
			.isAutoRegister(snsProviderMongodb.getIsAutoRegister())
			.metadata(CairoAppUserConverter.convertAppUser(snsProviderMongodb.getMetadata(), metadataUserMap))
			.build();
	}

	public static BasicSnsProvider convertBasicSnsProvider(SnsProviderMongodb snsProviderMongodb) {
		return BasicSnsProvider.builder()
			.snsProviderId(snsProviderMongodb.getSnsProviderId())
			.snsProviderName(snsProviderMongodb.getSnsProviderName())
			.snsProviderType(snsProviderMongodb.getSnsProviderType())
			.snsProviderPartner(snsProviderMongodb.getSnsProviderPartner())
			.enabled(snsProviderMongodb.getEnabled())
			.build();
	}
}
