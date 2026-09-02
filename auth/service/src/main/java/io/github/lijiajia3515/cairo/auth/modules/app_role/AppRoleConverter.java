package io.github.lijiajia3515.cairo.auth.modules.app_role;


import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRoleExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRoleField;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;

import java.util.Map;
import java.util.Optional;

public class AppRoleConverter {

	public static MetadataAppRole convert(AppRoleMongodb m, Map<String, Integer> userNumMap, Map<String, AppUser> metadataUserMap, AppRoleExtension extension) {
		final MetadataAppRole.MetadataAppRoleBuilder<?, ?> builder = MetadataAppRole.builder()
			.roleId(m.getRoleId())
			.enabled(m.getEnabled());

		Optional.of(extension.fields())
			.filter(x -> x.contains(AppRoleField.NAME))
			.flatMap(x -> Optional.ofNullable(m.getRoleName()))
			.ifPresent(builder::roleName);


		Optional.of(extension.fields())
			.filter(x -> x.contains(AppRoleField.REMARK))
			.flatMap(x -> Optional.ofNullable(m.getRemark()))
			.ifPresent(builder::remark);

		Optional.of(extension.fields())
			.filter(x -> x.contains(AppRoleField.USER_NUM))
			.map(x -> Optional.ofNullable(m.getRoleId()).filter(userNumMap::containsKey).map(userNumMap::get).orElse(0))
			.ifPresent(builder::userNum);

		Optional.of(extension.fields())
			.filter(x -> x.contains(AppRoleField.METADATA))
			.map(x -> CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.ifPresent(builder::metadata);

		return builder.build();
	}

	public static AppRole convert(AppRoleMongodb m) {
		return AppRole.builder()
			.roleId(m.getRoleId())
			.roleName(m.getRoleName())
			.remark(m.getRemark())
			.enabled(m.getEnabled())
			.build();
	}
}
