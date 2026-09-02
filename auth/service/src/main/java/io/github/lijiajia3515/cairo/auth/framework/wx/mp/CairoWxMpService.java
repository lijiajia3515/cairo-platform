package io.github.lijiajia3515.cairo.auth.framework.wx.mp;

import io.github.lijiajia3515.cairo.redis.CairoRedisProperties;
import io.github.lijiajia3515.cairo.auth.framework.wx.ConfigStorage;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxRuntimeException;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CairoWxMpService {
	private final WxRedisOps wxRedisOps;
	private final WxMpService wxMpService;
	private final CairoRedisProperties cairoRedisProperties;


	public CairoWxMpService(WxRedisOps wxRedisOps, WxMpService wxMpService, CairoRedisProperties cairoRedisProperties) {
		this.wxRedisOps = wxRedisOps;
		this.wxMpService = wxMpService;
		this.cairoRedisProperties = cairoRedisProperties;
	}

	/**
	 * 是否存在微信公众号配置
	 *
	 * @param snsProviderId 第三方认证提供商ID
	 * @return 是否存在
	 */
	public boolean existsConfig(String snsProviderId) {
		try {
			return wxMpService.switchoverTo(snsProviderId).getWxMpConfigStorage() != null;
		} catch (WxRuntimeException e) {
			log.info("getWxMpConfigStorage", e);
			return false;
		}
	}

	/**
	 * 获取微信公众号配置
	 *
	 * @param snsProviderId 第三方认证提供商ID
	 * @return 微信公众号配置 可能为空
	 */
	public WxMpConfigStorage getConfig(String snsProviderId) {
		return wxMpService.switchoverTo(snsProviderId).getWxMpConfigStorage();
	}

	/**
	 * 添加单个配置
	 *
	 * @param snsProviderId 第三方认证供应商ID
	 * @param properties    微信公众号配置文件
	 */
	public void addConfig(String snsProviderId, WxMpProperties properties) {
		wxMpService.addConfigStorage(snsProviderId, wxMpConfigStorage(snsProviderId, properties));
	}

	/**
	 * 移除公众号配置
	 *
	 * @param snsProviderId 第三方认证供应商ID
	 */
	public void removeConfig(String snsProviderId) {
		wxMpService.removeConfigStorage(snsProviderId);
	}

	/**
	 * 推送所有配置
	 *
	 * @param configs 所有配置文件
	 */
	public void pushConfigs(Map<String, WxMpProperties> configs) {
		Map<String, WxMpConfigStorage> wxMaConfigs = new HashMap<>();
		configs.forEach((mpId, properties) -> {
			wxMaConfigs.put(mpId, wxMpConfigStorage(mpId, properties));
		});
		wxMpService.setMultiConfigStorages(wxMaConfigs, "default");
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
