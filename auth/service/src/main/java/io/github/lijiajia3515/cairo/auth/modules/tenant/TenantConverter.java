package io.github.lijiajia3515.cairo.auth.modules.tenant;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.MetadataTenant;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;

import java.util.Map;
import java.util.Optional;

public class TenantConverter {

	public static Tenant convertBasicTenant(TenantMongodb m) {
		final Tenant.TenantBuilder<?, ?> builder = Tenant.builder()
			.tenantId(m.getTenantId())
			.tenantName(m.getTenantName())
			.icon(m.getIcon());

		return builder.build();
	}

	public static Tenant convertTenant(TenantMongodb m) {
		final Tenant.TenantBuilder<?, ?> builder = Tenant.builder()
			.tenantId(m.getTenantId())
			.tenantName(m.getTenantName())
			.aliasName(m.getAliasName())
			.icon(m.getIcon())
			.ownerAccount(Account.builder()
				.accountId(m.getOwnerAccountId())
				.nickname(m.getOwnerAccountId())
				.build()
			);

		return builder.build();
	}

	public static MetadataTenant convertMetadataTenant(TenantMongodb m, Map<String, Account> accountMap, Map<String, Account> metadataAccountMap) {
		final MetadataTenant.MetadataTenantBuilder<?, ?> builder = MetadataTenant.builder();
		builder
			.tenantId(m.getTenantId())
			.tenantName(m.getTenantName())
			.aliasName(m.getAliasName())
			.ownerAccount(Optional.ofNullable(accountMap.get(m.getOwnerAccountId()))
				.orElse(Account.builder()
					.accountId(m.getOwnerAccountId())
					.nickname(m.getOwnerAccountId())
					.build())
			)
			.icon(m.getIcon())
			.enabled(m.getEnabled())
			.metadata(CairoAccountConverter.convertAccount(m.getMetadata(), metadataAccountMap));

		return builder.build();
	}

}
