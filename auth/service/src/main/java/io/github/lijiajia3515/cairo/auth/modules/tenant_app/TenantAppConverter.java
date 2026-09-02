package io.github.lijiajia3515.cairo.auth.modules.tenant_app;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.MetadataTenantApp;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.TenantApp;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TenantAppConverter {

	public static MetadataTenantApp convertTenantApp(TenantAppMongodb m, Map<String, Tenant> tenantMap, Map<String, App> appMap, Map<String, Account> adminAccountMap, Map<String, Account> metadataAccountMap) {
		MetadataTenantApp.MetadataTenantAppBuilder<?, ?> builder = MetadataTenantApp.builder();
		builder
			.tenantId(m.getTenantId())
			.tenantName(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getTenantName).orElse(m.getTenantId()))
			.tenantIcon(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getIcon).orElse(null))
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
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
			.metadata(CairoAccountConverter.convertAccount(m.getMetadata(), metadataAccountMap))
			.build();
		return builder.build();
	}

	public static TenantApp convertTenantApp(TenantAppMongodb m, Map<String, Tenant> tenantMap, Map<String, App> appMap, Map<String, Account> adminAccountMap) {
		TenantApp.TenantAppBuilder<?, ?> builder = TenantApp.builder();
		builder
			.tenantId(m.getTenantId())
			.tenantName(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getTenantName).orElse(m.getTenantId()))
			.tenantIcon(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getIcon).orElse(null))
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
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
			//.enabled(m.getEnabled())
			.createTime(Optional.ofNullable(m.getMetadata()).map(AccountMetadataMongodb::getCreateTime).orElse(null))
			.build();
		return builder.build();
	}

}
