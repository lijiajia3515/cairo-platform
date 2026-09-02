package io.github.lijiajia3515.cairo.auth.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceOkHttpImpl;
import io.github.lijiajia3515.cairo.auth.framework.wx.ma.CairoWxMaService;
import io.github.lijiajia3515.cairo.auth.framework.wx.mp.CairoWxMpService;
import io.github.lijiajia3515.cairo.auth.framework.wx.mp.WxMpConfigFunction;
import io.github.lijiajia3515.cairo.auth.framework.wx.web.CairoWxWebService;
import io.github.lijiajia3515.cairo.auth.framework.wx.web.WxWebConfigFunction;
import io.github.lijiajia3515.cairo.redis.CairoRedisProperties;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceOkHttpImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
public class WxConfig {

	@Bean
	public WxRedisOps wxRedisOps(StringRedisTemplate redisTemplate) {
		return new RedisTemplateWxRedisOps(redisTemplate);
	}


	@Bean
	@Primary
	WxMpService wxMpService(WxRedisOps redisOps, CairoRedisProperties cairoRedisProperties, @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		WxMpServiceOkHttpImpl wxMpService = new WxMpServiceOkHttpImpl();
		WxMpConfigFunction configFunction = new WxMpConfigFunction(redisOps, cairoRedisProperties, readMongoTemplate);
		wxMpService.setConfigStorageFunction(configFunction);
		return wxMpService;
	}

	@Bean
	WxMpService wxWebService(WxRedisOps redisOps, @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		WxMpServiceOkHttpImpl wxWebService = new WxMpServiceOkHttpImpl();
		WxWebConfigFunction configFunction = new WxWebConfigFunction(redisOps, readMongoTemplate);
		wxWebService.setConfigStorageFunction(configFunction);
		return wxWebService;
	}

	@Bean
	WxMaService wxMaService(WxRedisOps redisOps) {
		WxMaServiceOkHttpImpl wxMaService = new WxMaServiceOkHttpImpl();
		return wxMaService;
	}

	@Bean
	CairoWxMpService cairoWxMpService(WxRedisOps wxRedisOps, WxMpService wxMpService, CairoRedisProperties cairoRedisProperties) {
		return new CairoWxMpService(wxRedisOps, wxMpService, cairoRedisProperties);
	}

	@Bean
	CairoWxWebService cairoWxWebService(WxRedisOps wxRedisOps, @Qualifier("wxWebService") WxMpService wxWebService) {
		return new CairoWxWebService(wxRedisOps, wxWebService);
	}

	@Bean
	CairoWxMaService cairoWxMaService(WxRedisOps wxRedisOps, WxMaService wxMaService) {
		return new CairoWxMaService(wxRedisOps, wxMaService);
	}
}
