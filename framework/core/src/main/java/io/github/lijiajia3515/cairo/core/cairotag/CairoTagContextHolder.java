package io.github.lijiajia3515.cairo.core.cairotag;

/**
 * 灰度标签存储
 */
public class CairoTagContextHolder {
	public static final String DEFAULT_TAG = "";
	private static final InheritableThreadLocal<String> HOLDER = new InheritableThreadLocal<>() {
		@Override
		protected String initialValue() {
			return DEFAULT_TAG;
		}
	};

	public static void setCairoTag(String cairoTag) {
		HOLDER.set(cairoTag);
	}

	public static String getCairoTag() {
		return HOLDER.get();
	}
}
