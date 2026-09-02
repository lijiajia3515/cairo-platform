package io.github.lijiajia3515.cairo.auth.framework.wx.mp.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsProviderMongodb;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsType;
import io.github.lijiajia3515.cairo.auth.framework.wx.mp.CairoWxMpService;
import io.github.lijiajia3515.cairo.auth.framework.wx.mp.WxMpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 微信公众号config
 */
@Slf4j
@Component
public class WxMpJob {
	private final CairoWxMpService cairoWxMpService;
	private final MongoTemplate readMongoTemplate;

	public WxMpJob(CairoWxMpService cairoWxMpService,
                   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.cairoWxMpService = cairoWxMpService;
		this.readMongoTemplate = readMongoTemplate;
	}


	@XxlJob("wxMpConfigJob")
	public void wxMpConfigJob() {
		XxlJobHelper.log("job start");
		try {
			//查询所有微信公众号配置信息
			Criteria criteria = Criteria.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_TYPE).is(SnsType.WX_MP.getTypeValue())
				.and(SnsProviderMongodb.FIELD.ENABLED).is(true);
			Query query = Query.query(criteria);
			List<SnsProviderMongodb> providerMongodbs = readMongoTemplate.find(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);

			providerMongodbs.forEach(provider -> {
				//移除配置
				cairoWxMpService.removeConfig(provider.getSnsProviderId());

				//重新添加配置,与数据库保持一致
				WxMpProperties wxMpProperties = new WxMpProperties();
				wxMpProperties.setAppId(provider.getClientId());
				wxMpProperties.setSecret(provider.getClientSecret());
				cairoWxMpService.addConfig(provider.getSnsProviderId(), wxMpProperties);
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
