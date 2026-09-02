package io.github.lijiajia3515.cairo.auth.modules.wxmp.provider;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.provider.MetadataWxmpProvider;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.provider.WxmpProvider;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpProviderMongodb;

import java.util.Map;


public class WxmpProviderConverter {

	public static WxmpProvider convertWxmpProvider(WxmpProviderMongodb mongo) {
		return WxmpProvider.builder()
			.wxmpProviderId(mongo.getWxmpProviderId())
			.wxmpProviderName(mongo.getWxmpProviderName())
			.wxmpToken(mongo.getWxmpToken())
			.wxmpAppId(mongo.getWxmpAppId())
			.wxmpSecret(mongo.getWxmpSecret())
			.wxmpAesKey(mongo.getWxmpAesKey())
			.enabled(mongo.isEnabled())
			.build();
	}

	public static MetadataWxmpProvider convertMetadataWxmpProvider(WxmpProviderMongodb mongo, Map<String, AppUser> metadataUserMap) {
		return MetadataWxmpProvider.builder()
			.wxmpProviderId(mongo.getWxmpProviderId())
			.wxmpProviderName(mongo.getWxmpProviderName())
			.wxmpToken(mongo.getWxmpToken())
			.wxmpAppId(mongo.getWxmpAppId())
			.wxmpSecret(mongo.getWxmpSecret())
			.wxmpAesKey(mongo.getWxmpAesKey())
			.enabled(mongo.isEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(mongo.getMetadata(), metadataUserMap))
			.build();
	}

}
