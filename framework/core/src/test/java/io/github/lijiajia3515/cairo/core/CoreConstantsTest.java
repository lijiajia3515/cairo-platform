package io.github.lijiajia3515.cairo.core;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UUIDv7 生成器契约:版本/变体位正确、时序不回退、批量唯一
 */
class CoreConstantsTest {

	/**
	 * 生成的字符串是合法 UUIDv7:version=7、variant=RFC 4122
	 */
	@Test
	void nextIdStrIsUuidV7() {
		UUID uuid = UUID.fromString(CoreConstants.nextIdStr());

		assertThat(uuid.version()).isEqualTo(7);
		assertThat(uuid.variant()).isEqualTo(2);
	}

	/**
	 * 时间戳前缀保证:后生成的 ID 字符串序不早于先生成的
	 */
	@Test
	void nextIdStrIsTimeOrdered() {
		String first = CoreConstants.nextIdStr();
		for (int i = 0; i < 1000; i++) {
			String next = CoreConstants.nextIdStr();
			assertThat(next.compareTo(first)).isGreaterThanOrEqualTo(0);
			first = next;
		}
	}

	/**
	 * 批量生成不重复
	 */
	@Test
	void nextIdStrIsUnique() {
		var ids = new java.util.HashSet<String>();
		for (int i = 0; i < 10_000; i++) {
			ids.add(CoreConstants.nextIdStr());
		}
		assertThat(ids).hasSize(10_000);
	}
}
