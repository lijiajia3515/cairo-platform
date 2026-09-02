package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.BasicTenantAppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.MetadataTenantAppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUserExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUserField;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_tag.TenantAppUserTag;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * tenant app user converter
 */
public class TenantAppUserConverter {

	public TenantAppUserConverter() {
	}

	public static TenantAppUser convertUser(TenantAppUserMongodb x, Map<String, TenantAppUserTag> userTagMap, Map<String, AccountMongodb> accountMap, Map<String, TenantAppRole> roleMap, Map<String, PathTenantAppDepartment> departmentMap, Set<String> appAdminAccountIds, TenantAppUserExtension extension) {
		TenantAppUser.TenantAppUserBuilder<?, ?> builder = TenantAppUser.builder();

		builder.userId(x.getUserId())
			.tenantId(x.getTenantId())
			.appId(x.getAppId())
			.joinTime(x.getJoinTime());

		if (extension.fields().contains(TenantAppUserField.NICKNAME)) {
			builder.nickname(Optional.ofNullable(x.getNickname()).orElse(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null)));
		}

		if (extension.fields().contains(TenantAppUserField.PHONE_NUMBER)) {
			builder.phoneNumber(x.getPhoneNumber());
		}

		if (extension.fields().contains(TenantAppUserField.ROLE)) {
			builder.roles(
					Optional.ofNullable(x.getRoleIds())
						.map(g -> g.stream().map(roleId -> roleMap.getOrDefault(
							roleId,
							TenantAppRole.builder()
								.roleId(roleId)
								.roleName(roleId)
								.build())
						).collect(Collectors.toList()))
						.orElse(Collections.emptyList())
				)
				.appAdmin(x.getAdmin());
		}

		if (extension.fields().contains(TenantAppUserField.DEPARTMENT)) {
			builder.departments(
				Optional.ofNullable(x.getDepartmentIds())
					.map(g -> g.stream().map(departmentId -> departmentMap.getOrDefault(
						departmentId,
						PathTenantAppDepartment.builder()
							.departmentIds(Collections.singletonList(departmentId))
							.departmentNames(Collections.singletonList(departmentId))
							.build())
					).collect(Collectors.toList()))
					.orElse(Collections.emptyList())
			);
			builder.mainDepartmentId(x.getMainDepartmentId());
		}


		if (extension.fields().contains(TenantAppUserField.POSITION)) {
			builder.position(x.getPosition());
		}

		if (extension.fields().contains(TenantAppUserField.TAG)) {
			List<TenantAppUserTag> userTags = Optional.ofNullable(x.getTagIds())
				.orElse(Collections.emptyList()).stream()
				.map(t -> userTagMap.getOrDefault(t, TenantAppUserTag.builder().tagId(t).tagName(t).build()))
				.collect(Collectors.toList());
			builder.tags(userTags);
		}

		if (extension.fields().contains(TenantAppUserField.USER_STATUS)) {
			builder
				.enabled(x.getEnabled())
				.logoffStatus(Optional.ofNullable(x.getLogoffStatus()).orElse(TenantAppUserLogoffStatus.NO.getLogoffStatusValue()))
				.logoffPendingTime(x.getLogoffPendingTime())
				.logoffSuccessTime(x.getLogoffSuccessTime());

		}

		if (extension.fields().contains(TenantAppUserField.LOGIN_TIME)) {
			builder.loginTime(x.getLoginTime());
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_ID)) {
			builder.accountId(x.getAccountId());
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_NICKNAME)) {
			builder.accountNickname(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_USERNAME)) {
			builder.accountUsername(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getUsername).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_PHONE_NUMBER)) {
			builder.accountPhoneNumber(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getPhoneNumber).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_EMAIL)) {
			builder.accountEmail(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getEmail).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_AVATAR_URL)) {
			builder.accountAvatarUrl(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getAvatarUrl).orElse(null));
		}

		return builder.build();
	}

	public static BasicTenantAppUser convertBasicTenantAppUser(TenantAppUserMongodb mongodb) {
		return BasicTenantAppUser.builder()
			.userId(mongodb.getUserId())
			.nickname(mongodb.getNickname())
			.build();
	}

	public static MetadataTenantAppUser convertMetadataUser(TenantAppUserMongodb x, Map<String, TenantAppUserTag> userTagMap, Map<String, AccountMongodb> accountMap, Map<String, TenantAppUser> metadataUserMap, Map<String, TenantAppRole> roleMap, Map<String, PathTenantAppDepartment> departmentMap, Set<String> appAdminAccountIds, TenantAppUserExtension extension) {
		MetadataTenantAppUser.MetadataTenantAppUserBuilder<?, ?> builder = MetadataTenantAppUser.builder();

		builder.userId(x.getUserId())
			.tenantId(x.getTenantId())
			.appId(x.getAppId())
			.joinTime(x.getJoinTime());

		if (extension.fields().contains(TenantAppUserField.NICKNAME)) {
			builder.nickname(Optional.ofNullable(x.getNickname()).orElse(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null)));
		}

		if (extension.fields().contains(TenantAppUserField.PHONE_NUMBER)) {
			builder.phoneNumber(x.getPhoneNumber());
		}

		if (extension.fields().contains(TenantAppUserField.ROLE)) {
			builder.roles(
					Optional.ofNullable(x.getRoleIds())
						.map(g -> g.stream().map(roleId -> roleMap.getOrDefault(
							roleId,
							TenantAppRole.builder()
								.roleId(roleId)
								.roleName(roleId)
								.build())
						).collect(Collectors.toList()))
						.orElse(Collections.emptyList())
				)
				.appAdmin(x.getAdmin());
		}

		if (extension.fields().contains(TenantAppUserField.DEPARTMENT)) {
			builder.departments(
				Optional.ofNullable(x.getDepartmentIds())
					.map(g -> g.stream().map(departmentMap::get).collect(Collectors.toList()))
					.orElse(Collections.emptyList())
			);
			builder.mainDepartmentId(x.getMainDepartmentId());
		}

		if (extension.fields().contains(TenantAppUserField.POSITION)) {
			builder.position(x.getPosition());
		}


		if (extension.fields().contains(TenantAppUserField.TAG)) {
			List<TenantAppUserTag> userTags = Optional.ofNullable(x.getTagIds())
				.orElse(Collections.emptyList()).stream()
				.map(t -> userTagMap.getOrDefault(t, TenantAppUserTag.builder().tagId(t).tagName(t).build()))
				.collect(Collectors.toList());
			builder.tags(userTags);
		}

		if (extension.fields().contains(TenantAppUserField.USER_STATUS)) {
			builder
				.enabled(x.getEnabled())
				.logoffStatus(Optional.ofNullable(x.getLogoffStatus()).orElse(TenantAppUserLogoffStatus.NO.getLogoffStatusValue()))
				.logoffPendingTime(x.getLogoffPendingTime())
				.logoffSuccessTime(x.getLogoffSuccessTime());
		}

		if (extension.fields().contains(TenantAppUserField.LOGIN_TIME)) {
			builder.loginTime(x.getLoginTime());
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_ID)) {
			builder.accountId(x.getAccountId());
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_NICKNAME)) {
			builder.accountNickname(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_USERNAME)) {
			builder.accountUsername(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getUsername).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_PHONE_NUMBER)) {
			builder.accountPhoneNumber(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getPhoneNumber).orElse(null));
		}
		if (extension.fields().contains(TenantAppUserField.ACCOUNT_EMAIL)) {
			builder.accountEmail(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getEmail).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserField.ACCOUNT_AVATAR_URL)) {
			builder.accountAvatarUrl(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getAvatarUrl).orElse(null));
		}

		if (extension.fields().contains(TenantAppUserField.METADATA)) {
			builder.metadata(CairoTenantAppUserConverter.convertTenantAppUser(x.getMetadata(), metadataUserMap));
		}
		return builder.build();
	}

	public static BasicTenantAppUser convertMetadataUser(TenantAppUserMongodb userMongodb) {
		return BasicTenantAppUser.builder()
			.userId(userMongodb.getUserId())
			.nickname(userMongodb.getNickname())
			.build();
	}

	public static String getName(String userId, Map<String, TenantAppUserMongodb> userMap, Map<String, AccountMongodb> accountMap) {
		return Optional.ofNullable(userMap.get(userId))
			.flatMap(x -> Optional.ofNullable(x.getNickname())
				.or(() -> Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname)))
			.orElse(userId);
	}
}
