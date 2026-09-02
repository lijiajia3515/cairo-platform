package io.github.lijiajia3515.cairo.auth.modules.account;

import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.AccountExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.AccountField;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.MetadataAccount;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;

import java.util.Map;
import java.util.Optional;

public class AccountConverter {
	public static Account convertAccount(AccountMongodb accountMongodb) {
		return Account.builder()
			.accountId(accountMongodb.getAccountId())
			.nickname(accountMongodb.getNickname())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.username(accountMongodb.getUsername())
			.phoneNumber(accountMongodb.getPhoneNumber())
			.email(accountMongodb.getEmail())
			.locked(accountMongodb.isLocked())
			.enabled(accountMongodb.isEnabled())
			.joinTime(accountMongodb.getJoinTime())
			.loginTime(accountMongodb.getLoginTime())
			.logoffStatus(accountMongodb.getLogoffStatus())
			.logoffPendingTime(accountMongodb.getLogoffPendingTime())
			.logoffSuccessTime(accountMongodb.getLogoffSuccessTime())

			.build();
	}

	public static Account convertAccount(AccountMongodb accountMongodb, AccountExtension extension) {
		Account.AccountBuilder builder = Account.builder()
			.accountId(accountMongodb.getAccountId());

		if (extension.fields().contains(AccountField.NICKNAME)) {
			builder.nickname(accountMongodb.getNickname());
		}
		if (extension.fields().contains(AccountField.AVATAR_URL)) {
			builder.avatarUrl(accountMongodb.getAvatarUrl());
		}
		if (extension.fields().contains(AccountField.PHONE_NUMBER)) {
			builder.phoneNumber(accountMongodb.getPhoneNumber());
		}
		if (extension.fields().contains(AccountField.USERNAME)) {
			builder.username(accountMongodb.getUsername());
		}
		if (extension.fields().contains(AccountField.EMAIL)) {
			builder.email(accountMongodb.getEmail());
		}
		if (extension.fields().contains(AccountField.STATUS)) {
			builder.enabled(accountMongodb.isEnabled());
			builder.locked(accountMongodb.isLocked());
			builder.logoffStatus(Optional.ofNullable(accountMongodb.getLogoffStatus()).orElse(AccountLogoffStatus.NO.getLogoffStatusValue()))
				.logoffPendingTime(accountMongodb.getLogoffPendingTime())
				.logoffSuccessTime(accountMongodb.getLogoffSuccessTime());
		}
		if (extension.fields().contains(AccountField.JOIN_TIME)) {
			builder.joinTime(accountMongodb.getJoinTime());
		}

		return builder.build();
	}

	public static MetadataAccount convertMetadataAccount(AccountMongodb accountMongodb, Map<String, Account> metadataAccountMap) {
		return MetadataAccount.builder()
			.accountId(accountMongodb.getAccountId())
			.nickname(accountMongodb.getNickname())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.username(accountMongodb.getUsername())
			.phoneNumber(accountMongodb.getPhoneNumber())
			.email(accountMongodb.getEmail())
			.enabled(accountMongodb.isEnabled())
			.locked(accountMongodb.isLocked())
			.joinTime(accountMongodb.getJoinTime())
			.loginTime(accountMongodb.getLoginTime())
			.logoffStatus(Optional.ofNullable(accountMongodb.getLogoffStatus()).orElse(AccountLogoffStatus.NO.getLogoffStatusValue()))
			.logoffPendingTime(accountMongodb.getLogoffPendingTime())
			.logoffSuccessTime(accountMongodb.getLogoffSuccessTime())
			.metadata(CairoAccountConverter.convertAccount(accountMongodb.getMetadata(), metadataAccountMap))
			.build();
	}
}
