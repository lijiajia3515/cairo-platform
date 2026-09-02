package io.github.lijiajia3515.cairo.auth.modules.app;

import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.AppUserMetadataApp;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.MetadataApp;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * app converter
 */
public class AppConverter {

	public static MetadataApp convertApp(AppMongodb m, Map<String, Account> adminAccountMap, Map<String, AppUser> metadataUserMap) {
		return MetadataApp.builder()
			.appId(m.getAppId())
			.appName(m.getAppName())
			.scopes(m.getScopes())
			.icon(m.getIcon())
			.privateApp(m.getPrivateApp())
			.adminAccounts(Optional
				.ofNullable(m.getAdminAccountIds())
				.orElse(Collections.emptyList())
				.stream()
				.map(accountId -> Optional.ofNullable(adminAccountMap.get(accountId)).orElse(Account.builder()
					.accountId(accountId)
					.nickname(accountId)
					.build()
				))
				.collect(Collectors.toList())
			)
			.autoRegister(m.getAutoRegister())
			.enabled(m.getEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static AppUserMetadataApp convertAppUserApp(AppMongodb m, Map<String, AppUser> metadataUserMap) {
		return AppUserMetadataApp.builder()
			.appId(m.getAppId())
			.appName(m.getAppName())
			.privateApp(m.getPrivateApp())
			.icon(m.getIcon())
			.enabled(m.getEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static App convertApp(AppMongodb m) {
		return App.builder()
			.appId(m.getAppId())
			.appName(m.getAppName())
			.privateApp(m.getPrivateApp())
			.icon(m.getIcon())
			.enabled(m.getEnabled())
			.build();
	}


}
