package io.github.lijiajia3515.cairo.auth.autoconfigure;

import io.github.lijiajia3515.cairo.auth.modules.auth_code.AuthCodeClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.auth_code.AuthCodeClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.captcha.CaptchaClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.captcha.CaptchaClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.sns_provider.SnsProviderClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.sns_provider.SnsProviderClientApiFeignClientFallbackFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.lijiajia3515.cairo.auth.modules.area.AreaClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.area.AreaClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.file.common_file.CommonFileClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.file.common_file.CommonFileClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.imgproxy.ImgproxyClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.imgproxy.ImgproxyClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.link.LinkClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.link.LinkClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.SmsMsgClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.SmsMsgClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.mass_msg.WxmpMassMsgClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.mass_msg.WxmpMassMsgClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.WxmpProviderClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.WxmpProviderClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.WxmpSendMsgClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.WxmpSendMsgClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg.WxmpTemplateMsgClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg.WxmpTemplateMsgClientApiFeignClientFallbackFactory;

/**
 * feign fallback configuration
 */
@Configuration(proxyBeanMethods = false)
public class CairoAuthSdkCoreFeignConfiguration {


	@Bean
	@ConditionalOnClass(CaptchaClientApiFeignClient.class)
	@ConditionalOnMissingBean(CaptchaClientApiFeignClientFallbackFactory.class)
	CaptchaClientApiFeignClientFallbackFactory captchaClientFeignClientFallbackFactory() {
		return new CaptchaClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(AuthCodeClientApiFeignClient.class)
	@ConditionalOnMissingBean(AuthCodeClientApiFeignClientFallbackFactory.class)
	AuthCodeClientApiFeignClientFallbackFactory authCodeClientFeignClientFallbackFactory() {
		return new AuthCodeClientApiFeignClientFallbackFactory();
	}


	// ===== request start =====


	@Bean
	@ConditionalOnClass(SnsProviderClientApiFeignClient.class)
	@ConditionalOnMissingBean(SnsProviderClientApiFeignClientFallbackFactory.class)
	SnsProviderClientApiFeignClientFallbackFactory snsProviderClientFeignClientFallbackFactory() {
		return new SnsProviderClientApiFeignClientFallbackFactory();
	}



	// ===== request end =====

	@Bean
	@ConditionalOnClass(AreaClientApiFeignClient.class)
	@ConditionalOnMissingBean(AreaClientApiFeignClientFallbackFactory.class)
    AreaClientApiFeignClientFallbackFactory areaClientFeignClientApiFallbackFactory() {
		return new AreaClientApiFeignClientFallbackFactory();
	}



	@Bean
	@ConditionalOnClass(CommonFileClientApiFeignClient.class)
	@ConditionalOnMissingBean(CommonFileClientApiFeignClientFallbackFactory.class)
    CommonFileClientApiFeignClientFallbackFactory commonFileClientApiFeignClientFallbackFactory() {
		return new CommonFileClientApiFeignClientFallbackFactory();
	}



	@Bean
	@ConditionalOnClass(PublicFileClientApiFeignClient.class)
	@ConditionalOnMissingBean(PublicFileClientApiFeignClientFallbackFactory.class)
    PublicFileClientApiFeignClientFallbackFactory publicFileClientApiFeignClientFallbackFactory() {
		return new PublicFileClientApiFeignClientFallbackFactory();
	}


	@Bean
	@ConditionalOnClass(LinkClientApiFeignClient.class)
	@ConditionalOnMissingBean(LinkClientApiFeignClientFallbackFactory.class)
    LinkClientApiFeignClientFallbackFactory linkClientApiFeignClientFallbackFactory() {
		return new LinkClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(ImgproxyClientApiFeignClient.class)
	@ConditionalOnMissingBean(ImgproxyClientApiFeignClientFallbackFactory.class)
    ImgproxyClientApiFeignClientFallbackFactory imgproxyClientApiFeignClientFallbackFactory() {
		return new ImgproxyClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(SmsMsgClientApiFeignClient.class)
	@ConditionalOnMissingBean(SmsMsgClientApiFeignClientFallbackFactory.class)
    SmsMsgClientApiFeignClientFallbackFactory smsMsgClientApiFeignClientFallbackFactory() {
		return new SmsMsgClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(WxmpMassMsgClientApiFeignClient.class)
	@ConditionalOnMissingBean(WxmpMassMsgClientApiFeignClientFallbackFactory.class)
    WxmpMassMsgClientApiFeignClientFallbackFactory wxmpMassMsgClientApiFeignClientFallbackFactory() {
		return new WxmpMassMsgClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(WxmpSendMsgClientApiFeignClient.class)
	@ConditionalOnMissingBean(WxmpSendMsgClientApiFeignClientFallbackFactory.class)
    WxmpSendMsgClientApiFeignClientFallbackFactory wxmpSendMsgClientApiFeignClientFallbackFactory() {
		return new WxmpSendMsgClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(WxmpTemplateMsgClientApiFeignClient.class)
	@ConditionalOnMissingBean(WxmpTemplateMsgClientApiFeignClientFallbackFactory.class)
	WxmpTemplateMsgClientApiFeignClientFallbackFactory wxmpTemplateMsgClientApiFeignClientFallbackFactory() {
		return new WxmpTemplateMsgClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(WxmpProviderClientApiFeignClient.class)
	@ConditionalOnMissingBean(WxmpProviderClientApiFeignClientFallbackFactory.class)
	WxmpProviderClientApiFeignClientFallbackFactory wxmpProviderClientApiFeignClientFallbackFactory() {
		return new WxmpProviderClientApiFeignClientFallbackFactory();
	}
}
