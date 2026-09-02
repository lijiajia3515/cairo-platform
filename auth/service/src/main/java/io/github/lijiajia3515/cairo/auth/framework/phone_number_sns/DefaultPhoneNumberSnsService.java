package io.github.lijiajia3515.cairo.auth.framework.phone_number_sns;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class DefaultPhoneNumberSnsService implements PhoneNumberSnsService {
	private final WxMaService wxMaService;

	public DefaultPhoneNumberSnsService(WxMaService wxMaService) {
		this.wxMaService = wxMaService;
	}

	@Override
	public PhoneNumberSnsInfo getPhoneNumberInfo(String snsProviderId, String snsCode) throws Exception {
		WxMaPhoneNumberInfo phoneNumberInfo = wxMaService.switchoverTo(snsProviderId).getUserService().getNewPhoneNoInfo(snsCode);

		log.info("phoneNumberInfo: {}", phoneNumberInfo);

		return PhoneNumberSnsInfo.builder()
			.phoneNumber(phoneNumberInfo.getPurePhoneNumber())
			.countryCode(phoneNumberInfo.getCountryCode())
			.time(LocalDateTime.now())
			.build();

	}
}
