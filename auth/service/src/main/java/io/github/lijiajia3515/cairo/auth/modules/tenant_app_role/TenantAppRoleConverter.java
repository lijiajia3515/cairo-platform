package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role;


import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.MetadataTenantAppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRoleExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRoleField;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;

import java.util.Map;
import java.util.Optional;

public class TenantAppRoleConverter {

	public static MetadataTenantAppRole convert(TenantAppRoleMongodb m, Map<String, Integer> userNumMap, Map<String, TenantAppUser> metadataUserMap, TenantAppRoleExtension extension) {
		final MetadataTenantAppRole.MetadataTenantAppRoleBuilder<?, ?> builder = MetadataTenantAppRole.builder()
			.tenantId(m.getTenantId())
			.appId(m.getAppId())
			.roleId(m.getRoleId())
			.enabled(m.getEnabled());

		Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppRoleField.NAME))
			.flatMap(x -> Optional.ofNullable(m.getRoleName()))
			.ifPresent(builder::roleName);


		Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppRoleField.REMARK))
			.flatMap(x -> Optional.ofNullable(m.getRemark()))
			.ifPresent(builder::remark);

		Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppRoleField.USER_NUM))
			.map(x -> Optional.ofNullable(m.getRoleId()).filter(userNumMap::containsKey).map(userNumMap::get).orElse(0))
			.ifPresent(builder::userNum);

		Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppRoleField.METADATA))
			.map(x -> CairoTenantAppUserConverter.convertTenantAppUser(m.getMetadata(), metadataUserMap))
			.ifPresent(builder::metadata);

		return builder.build();
	}

	public static TenantAppRole convert(TenantAppRoleMongodb m) {
		return TenantAppRole.builder()
			.tenantId(m.getTenantId())
			.appId(m.getAppId())
			.roleId(m.getRoleId())
			.roleName(m.getRoleName())
			.remark(m.getRemark())
			.enabled(m.getEnabled())
			.build();
	}
}
