package io.github.lijiajia3515.cairo.auth.framework.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * cairo context holder
 */
public class CairoContextHolder {
	private static final InheritableThreadLocal<Map<String, String>> HOLDER = new InheritableThreadLocal<>() {
		@Override
		protected Map<String, String> initialValue() {
			return new HashMap<>();
		}
	};

	public static void setValue(String key, String value) {
		HOLDER.get().put(key, value);
	}

	/**
	 * 请求结束清理上下文。
	 * 线程池复用线程,不清理会让下一个(非 @CairoContext)请求读到上一个请求的残留上下文。
	 */
	public static void clear() {
		HOLDER.remove();
	}

	/**
	 * 获取值
	 *
	 * @param key key
	 * @return value optional
	 */
	public static Optional<String> getValue(String key) {
		return Optional.ofNullable(HOLDER.get().get(key));
	}
}
