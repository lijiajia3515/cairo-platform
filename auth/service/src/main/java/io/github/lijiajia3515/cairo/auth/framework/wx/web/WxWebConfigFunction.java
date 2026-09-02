package io.github.lijiajia3515.cairo.auth.framework.wx.web;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsProviderMongodb;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsType;
import io.github.lijiajia3515.cairo.auth.framework.wx.ConfigStorage;
import io.github.lijiajia3515.cairo.auth.framework.wx.mp.WxMpProperties;
import io.github.lijiajia3515.cairo.redis.CairoRedisProperties;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.function.Function;

public class WxWebConfigFunction implements Function<String, WxMpConfigStorage> {
	private final WxRedisOps wxRedisOps;
	private final MongoTemplate readMongoTemplate;

	public WxWebConfigFunction(WxRedisOps wxRedisOps,  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.wxRedisOps = wxRedisOps;
		this.readMongoTemplate = readMongoTemplate;
	}

	@Override
	public WxMpConfigStorage apply(String mpId) {
		Criteria criteria = Criteria
			.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_ID).is(mpId)
			.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_TYPE).is(SnsType.WX_WEB.getTypeValue())
			.and(SnsProviderMongodb.FIELD.ENABLED).is(true);
		Query query = Query.query(criteria);
		SnsProviderMongodb snsProviderMongodb = readMongoTemplate.findOne(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
		if (snsProviderMongodb != null) {
			WxMpProperties wxMpProperties = new WxMpProperties();
			wxMpProperties.setAppId(snsProviderMongodb.getClientId());
			wxMpProperties.setSecret(snsProviderMongodb.getClientSecret());

			return wxMpConfigStorage(mpId, wxMpProperties);
		}
		return null;
	}

	private WxMpConfigStorage wxMpConfigStorage(String mpId, WxMpProperties wxMpProperties) {
		WxMpRedisConfigImpl wxMpConfig = new WxMpRedisConfigImpl(wxRedisOps,  "wx_web:" + mpId);

		ConfigStorage configStorageProperties = wxMpProperties.getConfigStorage();
		wxMpConfig.setAppId(wxMpProperties.getAppId());
		wxMpConfig.setSecret(wxMpProperties.getSecret());
		wxMpConfig.setToken(wxMpProperties.getToken());
		wxMpConfig.setAesKey(wxMpProperties.getAesKey());

		if (wxMpProperties.getHost() != null) {
			wxMpConfig.setHostConfig(wxMpProperties.getHost());
		}

		wxMpConfig.setHttpProxyHost(configStorageProperties.getHttpProxyHost());
		wxMpConfig.setHttpProxyUsername(configStorageProperties.getHttpProxyUsername());
		wxMpConfig.setHttpProxyPassword(configStorageProperties.getHttpProxyPassword());
		if (configStorageProperties.getHttpProxyPort() != null) {
			wxMpConfig.setHttpProxyPort(configStorageProperties.getHttpProxyPort());
		}
		return wxMpConfig;
	}
}
