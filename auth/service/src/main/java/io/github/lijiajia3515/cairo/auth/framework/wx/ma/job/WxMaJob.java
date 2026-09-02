package io.github.lijiajia3515.cairo.auth.framework.wx.ma.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsProviderMongodb;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsType;
import io.github.lijiajia3515.cairo.auth.framework.wx.ma.CairoWxMaService;
import io.github.lijiajia3515.cairo.auth.framework.wx.ma.WxMaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 微信小程序config
 */
@Slf4j
@Component
public class WxMaJob {
	private final CairoWxMaService cairoWxMaService;
	private final MongoTemplate readMongoTemplate;

	public WxMaJob(CairoWxMaService cairoWxMaService,
				   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.cairoWxMaService = cairoWxMaService;
		this.readMongoTemplate = readMongoTemplate;
	}


	/**
	 * 微信小程序配置
	 *
	 */
	@XxlJob("wxMaConfigJob")
	public void wxMaConfigJob() {
	    XxlJobHelper.log("job start");
	    try {
	        // 查询微信小程序配置列表
	        Criteria criteria = Criteria.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_TYPE).is(SnsType.WX_MA.getTypeValue())
	            .and(SnsProviderMongodb.FIELD.ENABLED).is(true);
	        Query query = Query.query(criteria);
	        List<SnsProviderMongodb> providerMongodbs = readMongoTemplate.find(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);

	        providerMongodbs.forEach(provider -> {
	            // 先移除旧的配置
	            cairoWxMaService.removeConfig(provider.getSnsProviderId());
	            // 添加新的配置信息
	            WxMaProperties wxMaProperties = new WxMaProperties();
	            wxMaProperties.setAppId(provider.getClientId());
	            wxMaProperties.setSecret(provider.getClientSecret());
	            cairoWxMaService.addConfig(provider.getSnsProviderId(), wxMaProperties);
	        });

	    } catch (Exception e) {
	        log.error("error{}", e.getMessage());
	        XxlJobHelper.log(e.getMessage());
	        XxlJobHelper.handleFail("job fail");
	        throw new RuntimeException(e);
	    }
	    XxlJobHelper.log("job end");
	}
}
