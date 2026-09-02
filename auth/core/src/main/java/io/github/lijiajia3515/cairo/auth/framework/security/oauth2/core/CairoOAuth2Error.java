package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * cairo 的 oauth2error实现
 */
@Data
@EqualsAndHashCode(callSuper = true)

@SuperBuilder(toBuilder = true)
public class CairoOAuth2Error extends BusinessResult<OAuth2Error> {

	public CairoOAuth2Error() {
	}

	public CairoOAuth2Error(String code, String message, OAuth2Error error) {
		super(code, message, error);
	}

	public CairoOAuth2Error(BusinessResultBuilder<OAuth2Error, ?, ?> b) {
		super(b);
	}
}
