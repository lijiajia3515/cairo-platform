package io.github.lijiajia3515.cairo.auth.modules.weboffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeTicketToken implements Serializable {
	private String mode;
	private String tenantId;

	private String appId;

	private String userId;

	private String write;

	public static WebOfficeTicketToken valueOf(String value) {
		try {
			String[] split = value.split("\\.");
			return WebOfficeTicketToken.builder()
				.mode(split[0])
				.tenantId(split[1])
				.appId(split[2])
				.userId(split[3])
				.write(split[4])
				.build();
		} catch (RuntimeException e) {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}
	}

	public String toToken() {
		return String.format("%s.%s.%s.%s.%s", mode, tenantId, appId, userId, write);
	}
}
