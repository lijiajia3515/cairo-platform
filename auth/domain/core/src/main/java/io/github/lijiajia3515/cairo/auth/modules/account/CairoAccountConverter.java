package io.github.lijiajia3515.cairo.auth.modules.account;

import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.CairoAccountMetadata;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;

import java.util.Map;
import java.util.Optional;

/**
 * 通用转换器
 */
public class CairoAccountConverter {

	public static CairoAccountMetadata convertAccount(AccountMetadataMongodb mongodb, Map<String, Account> metadataAccountMap) {
		return CairoAccountMetadata.builder()
			.createAccount(Optional.ofNullable(metadataAccountMap.get(mongodb.getCreateAccountId())).orElse(Account.builder()
				.accountId(mongodb.getCreateAccountId())
				.nickname(mongodb.getCreateAccountId())
				.build())
			)
			.updateAccount(Optional.ofNullable(metadataAccountMap.get(mongodb.getUpdateAccountId())).orElse(Account.builder()
				.accountId(mongodb.getUpdateAccountId())
				.nickname(mongodb.getUpdateAccountId())
				.build()))
			.createTime(mongodb.getCreateTime())
			.updateTime(mongodb.getUpdateTime())
			.build();
	}



}
