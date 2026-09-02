package io.github.lijiajia3515.cairo.auth.framework.sns;

import io.github.lijiajia3515.cairo.auth.framework.sns.exception.SnsAuthenticationNotSupportException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultCairoSnsService implements SnsService {
	private final List<SnsProvider> snsProviders;

	public DefaultCairoSnsService(List<SnsProvider> snsProviders) {
		this.snsProviders = snsProviders;
	}

	@Override
	public SnsInfo getSnsInfo(String snsType, String snsProviderId, String snsCode) {
		SnsInfo snsInfo = null;
		for (int i = 0; i < snsProviders.size(); i++) {
			SnsProvider face = snsProviders.get(i);
			if (face.supports(snsType)) {
				snsInfo = (face.getSnsInfo(snsProviderId, snsCode));
				break;
			}
			if (i == snsProviders.size() - 1) {
				throw new SnsAuthenticationNotSupportException("第三方账号认证类型不支持");
			}
		}
		return snsInfo;

	}
}
