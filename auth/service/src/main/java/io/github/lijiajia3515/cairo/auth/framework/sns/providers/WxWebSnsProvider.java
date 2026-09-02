package io.github.lijiajia3515.cairo.auth.framework.sns.providers;

import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsInfo;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsPartner;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsProvider;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsType;
import io.github.lijiajia3515.cairo.auth.framework.sns.exception.SnsAuthenticationCodeFailedException;
import io.github.lijiajia3515.cairo.mongodb.serial.SerialService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 微信公众号SNS提供方
 */
@Slf4j
@Component
public class WxWebSnsProvider implements SnsProvider {
	private final AuthProperties authProperties;
	private final WxMpService wxWebService;
	private final SerialService serialService;
	public static final String SNS_SERIAL_NAMESPACE = "default";

	public WxWebSnsProvider(AuthProperties authProperties, @Qualifier("wxWebService") WxMpService wxWebService, SerialService serialService) {
		this.authProperties = authProperties;
		this.wxWebService = wxWebService;
		this.serialService = serialService;
	}

	@Override
	public SnsInfo getSnsInfo(String snsProviderId, String snsCode) {
		try {
			WxOAuth2AccessToken accessToken = wxWebService.switchoverTo(snsProviderId).getOAuth2Service().getAccessToken(snsCode);
			if (accessToken == null || accessToken.getOpenId() == null) {
				throw new SnsAuthenticationCodeFailedException("微信网站读取用户信息失败");
			}

			WxOAuth2UserInfo userInfo = wxWebService.getOAuth2Service().getUserInfo(accessToken, "zh_CN");

			String nickname = userInfo.getNickname();

			if (nickname == null || nickname.isBlank()) {
				String serialKey = "sns_" + SnsPartner.WX.getPartnerId();
				nickname =  SnsPartner.WX.getNickname() + serialService.next(SNS_SERIAL_NAMESPACE, serialKey);
			}

			return SnsInfo.builder()
				.partnerId(SnsPartner.WX.getPartnerId())
				.providerId(snsProviderId)
				.partnerOpenId(Optional.ofNullable(accessToken.getUnionId()).orElse(accessToken.getOpenId()))
				.providerOpenId(accessToken.getOpenId())
				.nickName(nickname)
				.avatarUrl(Optional.ofNullable(userInfo.getHeadImgUrl()).filter(x -> !x.isBlank()).orElse(authProperties.getDefaultAvatarUrl()))
				.sex(Optional.ofNullable(userInfo.getSex()).map(String::valueOf).filter(x -> !x.isBlank()).orElse("0"))
				.build();
		} catch (WxErrorException e) {
			log.warn("微信异常", e);
			throw new SnsAuthenticationCodeFailedException("微信网站授权码错误");
		}
	}

	@Override
	public boolean supports(String snsType) {
		return SnsType.WX_WEB.getTypeValue().equals(snsType);
	}
}
