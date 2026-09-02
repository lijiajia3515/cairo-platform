package io.github.lijiajia3515.cairo.auth.framework.idempotent;

import cn.hutool.core.collection.ConcurrentHashSet;

import java.time.Duration;
import java.util.Set;

public class MemoryIdempotentService implements IdempotentService {
	private final Set<String> tokens = new ConcurrentHashSet<>();

	@Override
	public boolean check(String token, Duration timeout) {
		if (token == null) {
			throw new BadTokenIdempotentException("token 不能为空");
		}
		boolean flag = tokens.contains(token);

		if (!flag) {
			tokens.add(token);
			return true;
		}
		return false;
	}
}
