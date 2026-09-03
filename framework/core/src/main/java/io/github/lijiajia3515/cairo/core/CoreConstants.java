package io.github.lijiajia3515.cairo.core;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

/**
 * 平台级常量与 ID 生成
 */
public class CoreConstants {
	/**
	 * RFC 9562 UUIDv7 生成器(JUG,线程安全,同毫秒内单调递增)
	 */
	private static final NoArgGenerator UUID_V7 = Generators.timeBasedEpochGenerator();

	/**
	 * 生成 RFC 9562 UUIDv7 字符串
	 * <p>
	 * 48bit 毫秒时间戳前缀:按字符串排序即按创建时间排序,索引友好;
	 * 完全免协调(无 workerId 分配),无时钟回拨风险——取代雪花算法。
	 *
	 * @return 36 字符 UUIDv7 字符串(如 019f3fb0-df7e-763e-9c21-d0d8419cf4cf)
	 */
	public static String nextIdStr() {
		return UUID_V7.generate().toString();
	}
}
