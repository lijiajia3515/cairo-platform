package io.github.lijiajia3515.cairo.auth.domain.dto.sns;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum SnsTokenStatus {
	OK("0"),
	USED("1"),
	EXPIRED("2");

	@Getter
	private final String status;

	SnsTokenStatus(String status) {
		this.status = status;
	}

	public static Optional<SnsTokenStatus> statusOf(String status) {
		return Arrays.stream(values()).filter(x -> x.status.equals(status)).findFirst();
	}
}
