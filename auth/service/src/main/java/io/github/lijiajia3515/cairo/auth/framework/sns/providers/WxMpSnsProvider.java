package io.github.lijiajia3515.cairo.auth.framework.sns.providers;

import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsInfo;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsPartner;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsProvider;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsType;
import io.github.lijiajia3515.cairo.auth.framework.sns.exception.SnsAuthenticationCodeFailedException;
import io.github.lijiajia3515.cairo.mongodb.serial.SerialService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 微信公众号SNS提供方
 */
@Slf4j
@Component
public class WxMpSnsProvider implements SnsProvider {
	private final AuthProperties authProperties;
	private final WxMpService wxMpService;
	private final SerialService serialService;
	public static final String SNS_SERIAL_NAMESPACE = "default";

	public WxMpSnsProvider(AuthProperties authProperties, WxMpService wxMpService, SerialService serialService) {
		this.authProperties = authProperties;
		this.wxMpService = wxMpService;
		this.serialService = serialService;
	}

	@Override
	public SnsInfo getSnsInfo(String snsProviderId, String snsCode) {
		try {
			WxOAuth2AccessToken accessToken = wxMpService.switchoverTo(snsProviderId).getOAuth2Service().getAccessToken(snsCode);

			if (accessToken == null || accessToken.getOpenId() == null) {
				throw new SnsAuthenticationCodeFailedException("微信公众号读取用户信息失败");
			}

			String serialKey = "sns_" + SnsPartner.WX.getPartnerId();
			String nickname = SnsPartner.WX.getNickname() + serialService.next(SNS_SERIAL_NAMESPACE, serialKey);

			return SnsInfo.builder()
				.partnerId(SnsPartner.WX.getPartnerId())
				.providerId(snsProviderId)
				.partnerOpenId(Optional.ofNullable(accessToken.getUnionId()).orElse(accessToken.getOpenId()))
				.providerOpenId(accessToken.getOpenId())
				.nickName(nickname)
				.avatarUrl(authProperties.getDefaultAvatarUrl())
				.sex("0")
				.build();
		} catch (WxErrorException e) {
			log.warn("微信异常", e);
			throw new SnsAuthenticationCodeFailedException("微信公众号授权码错误");
		}
	}

	@Override
	public boolean supports(String snsType) {
		return SnsType.WX_MP.getTypeValue().equals(snsType);
	}
}
