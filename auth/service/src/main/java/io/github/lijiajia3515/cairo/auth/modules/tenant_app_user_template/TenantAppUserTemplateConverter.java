package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.BasicTenantAppUserTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.MetadataTenantAppUserTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplateExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplateField;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.PathTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * tenantAppUserTemplate converter
 */
public class TenantAppUserTemplateConverter {

	public static TenantAppUserTemplate convertTenantAppUserTemplate(TenantAppUserTemplateMongodb x,
																	 Map<String, TenantAppRoleTemplate> roleMap,
																	 Map<String, PathTenantAppDepartmentTemplate> departmentMap,
																	 Map<String, AccountMongodb> accountMap,
																	 TenantAppUserTemplateExtension extension) {
		TenantAppUserTemplate.TenantAppUserTemplateBuilder<?, ?> builder = TenantAppUserTemplate.builder();

		builder.tenantAppUserTemplateId(x.getTenantAppUserTemplateId());

		if (extension.fields().contains(TenantAppUserTemplateField.NICKNAME)) {
			builder.nickname(Optional.ofNullable(x.getNickname()).orElse(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null)));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.PHONE_NUMBER)) {
			builder.phoneNumber(x.getPhoneNumber());
		}


		if (extension.fields().contains(TenantAppUserTemplateField.ROLE)) {
			builder.tenantAppRoleTemplates(
					Optional.ofNullable(x.getTenantAppRoleTemplateIds())
						.map(g -> g.stream().map(roleMap::get).collect(Collectors.toList()))
						.orElse(Collections.emptyList())
				)
				.appAdmin(x.getAdmin());
		}

		if (extension.fields().contains(TenantAppUserTemplateField.DEPARTMENT)) {
			builder.tenantAppDepartmentTemplates(
				Optional.ofNullable(x.getTenantAppDepartmentTemplateIds())
					.map(g -> g.stream().map(departmentMap::get).collect(Collectors.toList()))
					.orElse(Collections.emptyList())
			);
			builder.tenantMainDepartmentTemplateId(x.getTenantMainDepartmentTemplateId());
		}

		if (extension.fields().contains(TenantAppUserTemplateField.POSITION)) {
			builder.position(x.getPosition());
		}


		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_ID)) {
			builder.accountId(x.getAccountId());
		}

		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_NICKNAME)) {
			builder.accountNickname(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_USERNAME)) {
			builder.accountUsername(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getUsername).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_PHONE_NUMBER)) {
			builder.accountPhoneNumber(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getPhoneNumber).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_EMAIL)) {
			builder.accountEmail(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getEmail).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_AVATAR_URL)) {
			builder.accountAvatarUrl(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getAvatarUrl).orElse(null));
		}

		return builder.build();
	}

	public static MetadataTenantAppUserTemplate convertMetadataTenantAppUserTemplate(TenantAppUserTemplateMongodb x,
																					 Map<String, TenantAppRoleTemplate> roleMap,
																					 Map<String, PathTenantAppDepartmentTemplate> departmentMap,
																					 Map<String, AccountMongodb> accountMap,
																					 Map<String, AppUser> metadataUserMap,
																					 TenantAppUserTemplateExtension extension) {
		MetadataTenantAppUserTemplate.MetadataTenantAppUserTemplateBuilder<?, ?> builder = MetadataTenantAppUserTemplate.builder();

		builder.tenantAppUserTemplateId(x.getTenantAppUserTemplateId())
			.enabled(x.getEnabled());

		if (extension.fields().contains(TenantAppUserTemplateField.NICKNAME)) {
			builder.nickname(Optional.ofNullable(x.getNickname()).orElse(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null)));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.PHONE_NUMBER)) {
			builder.phoneNumber(x.getPhoneNumber());
		}


		if (extension.fields().contains(TenantAppUserTemplateField.ROLE)) {
			builder.tenantAppRoleTemplates(
					Optional.ofNullable(x.getTenantAppRoleTemplateIds())
						.map(g -> g.stream().map(roleId -> roleMap.getOrDefault(
							roleId,
							TenantAppRoleTemplate.builder()
								.tenantAppRoleTemplateId(roleId)
								.tenantAppRoleTemplateName(roleId)
								.build())
						).collect(Collectors.toList()))
						.orElse(Collections.emptyList())
				)
				.appAdmin(x.getAdmin());
		}

		if (extension.fields().contains(TenantAppUserTemplateField.DEPARTMENT)) {
			builder.tenantAppDepartmentTemplates(
				Optional.ofNullable(x.getTenantAppDepartmentTemplateIds())
					.map(g -> g.stream().map(departmentId -> departmentMap.getOrDefault(
						departmentId,
						PathTenantAppDepartmentTemplate.builder()
							.tenantAppDepartmentTemplateIds(Collections.singletonList(departmentId))
							.tenantAppDepartmentTemplateNames(Collections.singletonList(departmentId))
							.build())
					).collect(Collectors.toList()))
					.orElse(Collections.emptyList())
			);
			builder.tenantMainDepartmentTemplateId(x.getTenantMainDepartmentTemplateId());
		}

		if (extension.fields().contains(TenantAppUserTemplateField.POSITION)) {
			builder.position(x.getPosition());
		}


		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_ID)) {
			builder.accountId(x.getAccountId());
		}


		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_NICKNAME)) {
			builder.accountNickname(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_USERNAME)) {
			builder.accountUsername(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getUsername).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_PHONE_NUMBER)) {
			builder.accountPhoneNumber(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getPhoneNumber).orElse(null));
		}
		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_EMAIL)) {
			builder.accountEmail(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getEmail).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.ACCOUNT_AVATAR_URL)) {
			builder.accountAvatarUrl(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getAvatarUrl).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserTemplateField.METADATA)) {
			builder.metadata(CairoAppUserConverter.convertAppUser(x.getMetadata(), metadataUserMap));
		}
		return builder.build();
	}

	public static BasicTenantAppUserTemplate convertBasicTenantAppUserTemplate(TenantAppUserTemplateMongodb tenantAppUserTemplateMongodb) {
		return BasicTenantAppUserTemplate.builder()
			.tenantAppUserTemplateId(tenantAppUserTemplateMongodb.getTenantAppUserTemplateId())
			.nickname(tenantAppUserTemplateMongodb.getNickname())
			.build();
	}

	public static String getName(String appTenantAppUserTemplateId, Map<String, TenantAppUserTemplateMongodb> appTenantAppUserTemplateMap, Map<String, AccountMongodb> accountMap) {
		return Optional.ofNullable(appTenantAppUserTemplateMap.get(appTenantAppUserTemplateId))
			.flatMap(x -> Optional.ofNullable(x.getNickname())
				.or(() -> Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname)))
			.orElse(appTenantAppUserTemplateId);
	}
}
