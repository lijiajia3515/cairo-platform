package io.github.lijiajia3515.cairo.auth.modules.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StringUtils {
	public static Map<String, String> str2Map(String str) {
		Map<String, String> map = new HashMap<>();
		if (str == null || str.isEmpty()) return map;
		Arrays.stream(str.split(";")).forEach(item -> {
			String[] itemArr = item.split(":");
			if (itemArr.length == 2) {
				map.put(itemArr[0], itemArr[1]);
			}
		});
		return map;
	}

	public static String map2Str(Map<String, String> map) {
		StringBuffer sb = new StringBuffer();
		map.forEach((k, v) -> {
			sb.append(String.format("%s:%s;", k, v));
		});
		return sb.toString();
	}
}
