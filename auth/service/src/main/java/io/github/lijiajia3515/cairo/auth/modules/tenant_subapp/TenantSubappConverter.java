package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.MetadataTenantSubapp;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.TenantSubapp;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;

import java.util.Map;
import java.util.Optional;

public class TenantSubappConverter {

	public static MetadataTenantSubapp convertTenantSubapp(TenantSubappMongodb m,
																   Map<String, Tenant> tenantMap,
																   Map<String, App> appMap,
																   Map<String, Endpoint> endpointMap,
																   Map<String, Subapp> subappMap,
																   Map<String, Account> metadataAccountMap) {
		MetadataTenantSubapp.MetadataTenantSubappBuilder<?, ?> builder = MetadataTenantSubapp.builder();
		builder
			.tenantId(m.getTenantId())
			.tenantName(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getTenantName).orElse(m.getTenantId()))
			.tenantIcon(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getIcon).orElse(null))
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.enabled(m.getEnabled())
			.subappId(m.getSubappId())
			.subappName(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappName).orElse(m.getSubappId()))
			.metadata(CairoAccountConverter.convertAccount(m.getMetadata(), metadataAccountMap))
			.build();
		return builder.build();
	}

	public static TenantSubapp convertTenantSubapp(TenantSubappMongodb m,
														   Map<String, Tenant> tenantMap,
														   Map<String, App> appMap,
														   Map<String, Endpoint> endpointMap,
														   Map<String, Subapp> subappMap) {
		TenantSubapp.TenantSubappBuilder<?, ?> builder = TenantSubapp.builder();
		builder
			.tenantId(m.getTenantId())
			.tenantName(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getTenantName).orElse(m.getTenantId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.tenantIcon(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getIcon).orElse(null))
			.subappId(m.getSubappId())
			.subappName(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappName).orElse(m.getSubappId()))
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null));

		return builder.build();
	}

}
