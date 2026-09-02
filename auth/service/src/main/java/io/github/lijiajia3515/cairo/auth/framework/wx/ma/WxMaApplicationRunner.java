package io.github.lijiajia3515.cairo.auth.framework.wx.ma;


import cn.binarywang.wx.miniapp.api.WxMaService;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsProviderMongodb;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsPartner;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsType;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class WxMaApplicationRunner implements ApplicationRunner {
	private final CairoWxMaService cairoWxMaService;
	private final MongoTemplate readMongoTemplate;
	private final WxMaService wxMaService;

	public WxMaApplicationRunner(CairoWxMaService cairoWxMaService,
								 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								 WxMaService wxMaService) {
		this.cairoWxMaService = cairoWxMaService;
		this.readMongoTemplate = readMongoTemplate;
		this.wxMaService = wxMaService;
	}


	/**
	 * 启动时执行配置微信小程序
	 *
	 */
	@Override
	public void run(ApplicationArguments args) {
	    // 查询所有微信小程序提供方
	    Criteria criteria = Criteria.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_PARTNER).is(SnsPartner.WX)
	            .and(SnsProviderMongodb.FIELD.SNS_PROVIDER_TYPE).is(SnsType.WX_MA.getTypeValue())
	            .and(SnsProviderMongodb.FIELD.ENABLED).is(true);
	    Query query = Query.query(criteria);
	    List<SnsProviderMongodb> providerMongodbs = readMongoTemplate.find(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);

	    // 遍历查询结果，为每个提供商配置信息
	    providerMongodbs.forEach(provider->{
	        // 检查当前提供方的配置是否已存在
	        boolean existsConfig = cairoWxMaService.existsConfig(provider.getSnsProviderId());
	        // 如果配置不存在，则创建并添加新的配置
	        if (!existsConfig) {
	            WxMaProperties wxMaProperties=new WxMaProperties();
	            wxMaProperties.setAppId(provider.getClientId());
	            wxMaProperties.setSecret(provider.getClientSecret());
	            cairoWxMaService.addConfig(provider.getSnsProviderId(),wxMaProperties);
	        }
	        // 尝试切换到当前提供方，并获取访问令牌，用于验证配置的有效性
	        try {
	            wxMaService.switchoverTo(provider.getSnsProviderId()).getAccessToken();
	        } catch (WxErrorException e) {
	            log.error("wx_ma getAccessToken error {}",e.getMessage());
	        }
	    });

	}
}
