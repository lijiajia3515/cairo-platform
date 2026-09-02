package io.github.lijiajia3515.cairo.auth.framework.wx.mp;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpProviderMongodb;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
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
public class WxMpApplicationRunner implements ApplicationRunner {
	private final CairoWxMpService cairoWxMpService;
	private final MongoTemplate readMongoTemplate;
	private final WxMpService wxMpService;

	public WxMpApplicationRunner(CairoWxMpService cairoWxMpService,
                                 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
                                 WxMpService wxMpService) {
		this.cairoWxMpService = cairoWxMpService;
		this.readMongoTemplate = readMongoTemplate;
		this.wxMpService = wxMpService;
	}


	/**
	 * 启动时执行的逻辑，配置微信公众号
	 *
	 */
	@Override
	public void run(ApplicationArguments args) {
	    // 查询所有微信公众号配置
	    Criteria criteria = Criteria.where(WxmpProviderMongodb.FIELD.ENABLED).is(true);

	    Query query = Query.query(criteria);
	    List<WxmpProviderMongodb> providerMongodbs = readMongoTemplate.find(query, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);

	    providerMongodbs.forEach(provider -> {
	        // 检查当前公众号配置是否存在
	        boolean existsConfig = cairoWxMpService.existsConfig(provider.getWxmpProviderId());
	        // 如果配置不存在，则创建新的配置
	        if (!existsConfig) {
	            WxMpProperties wxMpProperties = new WxMpProperties();
				wxMpProperties.setAppId(provider.getWxmpAppId());
				wxMpProperties.setSecret(provider.getWxmpSecret());
				wxMpProperties.setToken(provider.getWxmpToken());
				wxMpProperties.setAesKey(provider.getWxmpAesKey());
	            cairoWxMpService.addConfig(provider.getWxmpProviderId(), wxMpProperties);
	        }
	        try {
	            // 尝试切换到当前公众号，并获取访问凭证，用于验证配置是否有效
	            wxMpService.switchoverTo(provider.getWxmpProviderId()).getAccessToken();
	        } catch (WxErrorException e) {
	            log.error("wx_mp getAccessToken error {}", e.getMessage());
	        }
	    });

	}
}
