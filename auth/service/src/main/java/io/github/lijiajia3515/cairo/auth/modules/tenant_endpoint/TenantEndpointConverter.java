package io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.MetadataTenantEndpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.TenantEndpoint;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;

import java.util.Map;
import java.util.Optional;

public class TenantEndpointConverter {

	public static MetadataTenantEndpoint convertTenantEndpoint(TenantEndpointMongodb m, Map<String, Tenant> tenantMap, Map<String, App> appMap, Map<String, Endpoint> endpointMap, Map<String, Account> metadataAccountMap) {
		MetadataTenantEndpoint.MetadataTenantEndpointBuilder<?, ?> builder = MetadataTenantEndpoint.builder();
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
			.metadata(CairoAccountConverter.convertAccount(m.getMetadata(), metadataAccountMap))
			.build();
		return builder.build();
	}

	public static TenantEndpoint convertTenantEndpoint(TenantEndpointMongodb m, Map<String, Tenant> tenantMap, Map<String, App> appMap, Map<String, Endpoint> endpointMap) {
		TenantEndpoint.TenantEndpointBuilder<?, ?> builder = TenantEndpoint.builder();
		builder
			.tenantId(m.getTenantId())
			.tenantName(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getTenantName).orElse(m.getTenantId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.tenantIcon(Optional.ofNullable(tenantMap.get(m.getTenantId())).map(Tenant::getIcon).orElse(null))
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))

			.createTime(Optional.ofNullable(m.getMetadata()).map(AccountMetadataMongodb::getCreateTime).orElse(null))
		;

		return builder.build();
	}

}
