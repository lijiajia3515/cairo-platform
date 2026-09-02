package io.github.lijiajia3515.cairo.auth.modules.app_user;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUserExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUserField;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUserMetadata;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.BasicAppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.PathAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_tag.AppUserTag;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * appAppUser converter
 */
public class AppUserConverter {

	public AppUserConverter() {
	}

	public static AppUser convertAppUser(AppUserMongodb x,
										 Map<String, MetadataAppRole> roleMap,
										 Map<String, AppDepartment> departmentMap,
										 Map<String, AppUserTag> appAppUserTagMap,
										 Map<String, AccountMongodb> accountMap,
										 AppUserExtension extension) {
		AppUser.AppUserBuilder<?, ?> builder = AppUser.builder();

		builder.userId(x.getUserId())
			.joinTime(x.getJoinTime());

		if (extension.fields().contains(AppUserField.NICKNAME)) {
			builder.nickname(Optional.ofNullable(x.getNickname()).orElse(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null)));
		}

		if (extension.fields().contains(AppUserField.PHONE_NUMBER)) {
			builder.phoneNumber(x.getPhoneNumber());
		}


		if (extension.fields().contains(AppUserField.ROLE)) {
			builder.roles(
					Optional.ofNullable(x.getRoleIds())
						.map(g -> g.stream().map(roleMap::get).collect(Collectors.toList()))
						.orElse(Collections.emptyList())
				)
				.appAdmin(x.getAdmin());
		}

		if (extension.fields().contains(AppUserField.DEPARTMENT)) {
			builder.departments(
				Optional.ofNullable(x.getDepartmentIds())
					.map(g -> g.stream().map(departmentMap::get).collect(Collectors.toList()))
					.orElse(Collections.emptyList())
			);
			builder.mainDepartmentId(x.getMainDepartmentId());
		}

		if (extension.fields().contains(AppUserField.POSITION)) {
			builder.position(x.getPosition());
		}


		if (extension.fields().contains(AppUserField.TAG)) {
			List<AppUserTag> appAppUserTags = Optional.ofNullable(x.getTagIds())
				.orElse(Collections.emptyList()).stream()
				.map(t -> appAppUserTagMap.getOrDefault(t, AppUserTag.builder().tagId(t).tagName(t).build()))
				.collect(Collectors.toList());
			builder.tags(appAppUserTags);
		}

		if (extension.fields().contains(AppUserField.USER_STATUS)) {
			builder
				.enabled(x.getEnabled())
				.logoffStatus(Optional.ofNullable(x.getLogoffStatus()).orElse(AppUserLogoffStatus.NO.getLogoffStatusValue()))
				.logoffPendingTime(x.getLogoffPendingTime())
				.logoffSuccessTime(x.getLogoffSuccessTime());
		}

		if (extension.fields().contains(AppUserField.LOGIN_TIME)) {
			builder.loginTime(x.getLoginTime());
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_ID)) {
			builder.accountId(x.getAccountId());
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_NICKNAME)) {
			builder.accountNickname(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null));
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_USERNAME)) {
			builder.accountUsername(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getUsername).orElse(null));
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_PHONE_NUMBER)) {
			builder.accountPhoneNumber(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getPhoneNumber).orElse(null));
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_EMAIL)) {
			builder.accountEmail(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getEmail).orElse(null));
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_AVATAR_URL)) {
			builder.accountAvatarUrl(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getAvatarUrl).orElse(null));
		}

		return builder.build();
	}

	public static AppUserMetadata convertMetadataAppUser(AppUserMongodb x,
														 Map<String, AppRole> roleMap,
														 Map<String, PathAppDepartment> departmentMap,
														 Map<String, AppUserTag> appAppUserTagMap,
														 Map<String, AccountMongodb> accountMap,
														 Map<String, AppUser> metadataAppUserMap,
														 AppUserExtension extension) {
		AppUserMetadata.AppUserMetadataBuilder<?, ?> builder = AppUserMetadata.builder();

		builder.userId(x.getUserId())
			.joinTime(x.getJoinTime());

		if (extension.fields().contains(AppUserField.NICKNAME)) {
			builder.nickname(Optional.ofNullable(x.getNickname()).orElse(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null)));
		}

		if (extension.fields().contains(AppUserField.PHONE_NUMBER)) {
			builder.phoneNumber(x.getPhoneNumber());
		}


		if (extension.fields().contains(AppUserField.ROLE)) {
			builder.roles(
					Optional.ofNullable(x.getRoleIds())
						.map(g -> g.stream().map(roleId -> roleMap.getOrDefault(
							roleId,
							AppRole.builder()
								.roleId(roleId)
								.roleName(roleId)
								.build())
						).collect(Collectors.toList()))
						.orElse(Collections.emptyList())
				)
				.appAdmin(x.getAdmin());
		}

		if (extension.fields().contains(AppUserField.DEPARTMENT)) {
			builder.departments(
				Optional.ofNullable(x.getDepartmentIds())
					.map(g -> g.stream().map(departmentId -> departmentMap.getOrDefault(
						departmentId,
						PathAppDepartment.builder()
							.departmentIds(Collections.singletonList(departmentId))
							.departmentNames(Collections.singletonList(departmentId))
							.build())
					).collect(Collectors.toList()))
					.orElse(Collections.emptyList())
			);
			builder.mainDepartmentId(x.getMainDepartmentId());
		}

		if (extension.fields().contains(AppUserField.POSITION)) {
			builder.position(x.getPosition());
		}


		if (extension.fields().contains(AppUserField.TAG)) {
			List<AppUserTag> userTags = Optional.ofNullable(x.getTagIds())
				.orElse(Collections.emptyList()).stream()
				.map(t -> appAppUserTagMap.getOrDefault(t, AppUserTag.builder().tagId(t).tagName(t).build()))
				.collect(Collectors.toList());
			builder.tags(userTags);
		}

		if (extension.fields().contains(AppUserField.USER_STATUS)) {
			builder
				.enabled(x.getEnabled())
				.logoffStatus(Optional.ofNullable(x.getLogoffStatus()).orElse(AppUserLogoffStatus.NO.getLogoffStatusValue()))
				.logoffPendingTime(x.getLogoffPendingTime())
				.logoffSuccessTime(x.getLogoffSuccessTime())
			;
		}

		if (extension.fields().contains(AppUserField.LOGIN_TIME)) {
			builder.loginTime(x.getLoginTime());
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_ID)) {
			builder.accountId(x.getAccountId());
		}


		if (extension.fields().contains(AppUserField.ACCOUNT_NICKNAME)) {
			builder.accountNickname(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname).orElse(null));
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_USERNAME)) {
			builder.accountUsername(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getUsername).orElse(null));
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_PHONE_NUMBER)) {
			builder.accountPhoneNumber(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getPhoneNumber).orElse(null));
		}
		if (extension.fields().contains(AppUserField.ACCOUNT_EMAIL)) {
			builder.accountEmail(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getEmail).orElse(null));
		}

		if (extension.fields().contains(AppUserField.ACCOUNT_AVATAR_URL)) {
			builder.accountAvatarUrl(Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getAvatarUrl).orElse(null));
		}

		if (extension.fields().contains(AppUserField.METADATA)) {
			builder.metadata(CairoAppUserConverter.convertAppUser(x.getMetadata(), metadataAppUserMap));
		}
		return builder.build();
	}

	public static BasicAppUser convertBasicAppUser(AppUserMongodb appAppUserMongodb) {
		return BasicAppUser.builder()
			.userId(appAppUserMongodb.getUserId())
			.nickname(appAppUserMongodb.getNickname())
			.build();
	}

	public static String getName(String appAppUserId, Map<String, AppUserMongodb> appAppUserMap, Map<String, AccountMongodb> accountMap) {
		return Optional.ofNullable(appAppUserMap.get(appAppUserId))
			.flatMap(x -> Optional.ofNullable(x.getNickname())
				.or(() -> Optional.ofNullable(accountMap.get(x.getAccountId())).map(AccountMongodb::getNickname)))
			.orElse(appAppUserId);
	}
}
