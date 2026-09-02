package io.github.lijiajia3515.cairo.auth.framework.wx.mp;

import io.github.lijiajia3515.cairo.redis.CairoRedisProperties;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpProviderMongodb;
import io.github.lijiajia3515.cairo.auth.framework.wx.ConfigStorage;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.function.Function;

public class WxMpConfigFunction implements Function<String, WxMpConfigStorage> {
	private final WxRedisOps wxRedisOps;
	private final CairoRedisProperties cairoRedisProperties;
	private final MongoTemplate readMongoTemplate;

	public WxMpConfigFunction(WxRedisOps wxRedisOps, CairoRedisProperties cairoRedisProperties, @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.wxRedisOps = wxRedisOps;
		this.cairoRedisProperties = cairoRedisProperties;
		this.readMongoTemplate = readMongoTemplate;
	}

	@Override
	public WxMpConfigStorage apply(String mpId) {
		Criteria criteria = Criteria
			.where(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).is(mpId)
			.and(WxmpProviderMongodb.FIELD.ENABLED).is(true);
		Query query = Query.query(criteria);
		WxmpProviderMongodb providerMongodb = readMongoTemplate.findOne(query, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);
		if (providerMongodb != null) {
			WxMpProperties wxMpProperties = new WxMpProperties();
			wxMpProperties.setAppId(providerMongodb.getWxmpAppId());
			wxMpProperties.setSecret(providerMongodb.getWxmpSecret());
			wxMpProperties.setToken(providerMongodb.getWxmpToken());
			wxMpProperties.setAesKey(providerMongodb.getWxmpAesKey());

			return wxMpConfigStorage(mpId, wxMpProperties);
		}
		return null;
	}

	private WxMpConfigStorage wxMpConfigStorage(String mpId, WxMpProperties wxMpProperties) {
		WxMpRedisConfigImpl wxMpConfig = new WxMpRedisConfigImpl(wxRedisOps, cairoRedisProperties.getKeyPrefix() + "wx_mp:" + mpId);

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
