package io.github.lijiajia3515.cairo.auth.modules.account;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CairoAccountTool {
	/**
	 * 获取metadata中accountId
	 *
	 * @param metadata metadata
	 * @return accountId-Stream
	 */
	public static Stream<String> getMetadataAccountIds(AccountMetadataMongodb metadata) {
		return Stream.of(metadata.getCreateAccountId(), metadata.getUpdateAccountId()).filter(Objects::nonNull);
	}


	public static Set<String> getAccountMetadataAccountIds(Collection<AccountMetadataMongodb> metadata) {
		return metadata.stream().flatMap(x -> Stream.of(x.getCreateAccountId(), x.getUpdateAccountId())).filter(Objects::nonNull).collect(Collectors.toSet());
	}
}
