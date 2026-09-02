package io.github.lijiajia3515.cairo.auth.framework.wx.ma;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import io.github.lijiajia3515.cairo.auth.framework.wx.ConfigStorage;
import me.chanjar.weixin.common.redis.WxRedisOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CairoWxMaService {
	private static final Logger log = LoggerFactory.getLogger(CairoWxMaService.class);
	private final WxRedisOps wxRedisOps;
	private final WxMaService wxMaService;

	public CairoWxMaService(WxRedisOps wxRedisOps, WxMaService wxMaService) {
		this.wxRedisOps = wxRedisOps;
		this.wxMaService = wxMaService;
	}

	/**
	 * 是否存在微信小程序配置
	 *
	 * @param snsProviderId 第三方认证提供商ID
	 * @return 是否存在
	 */
	public boolean existsConfig(String snsProviderId) {
		try {
			return wxMaService.switchoverTo(snsProviderId).getWxMaConfig() != null;
		} catch (Exception e) {
			log.info("getWxMaConfig", e);
			return false;
		}
	}

	/**
	 * 获取微信小程序配置
	 *
	 * @param snsProviderId 第三方认证提供商ID
	 * @return 微信小程序配置 可能为空
	 */
	public WxMaConfig getConfig(String snsProviderId) {
		return wxMaService.switchoverTo(snsProviderId).getWxMaConfig();
	}

	/**
	 * 添加单个配置
	 *
	 * @param snsProviderId 第三方认证供应商ID
	 * @param properties    配置文件
	 */
	public void addConfig(String snsProviderId, WxMaProperties properties) {
		wxMaService.addConfig(snsProviderId, wxMaConfig(snsProviderId, properties));
	}

	/**
	 * 移除配置
	 *
	 * @param snsProviderId 第三方认证供应商ID
	 */
	public void removeConfig(String snsProviderId) {
		wxMaService.removeConfig(snsProviderId);
	}

	/**
	 * 推送所有配置
	 *
	 * @param configs 所有配置文件
	 */
	public void pushConfigs(Map<String, WxMaProperties> configs) {
		Map<String, WxMaConfig> wxMaConfigs = new HashMap<>();
		configs.forEach((maId, properties) -> {
			wxMaConfigs.put(maId, wxMaConfig(maId, properties));
		});
		wxMaService.setMultiConfigs(wxMaConfigs);
	}

	private WxMaConfig wxMaConfig(String maId, WxMaProperties wxMaProperties) {
		ConfigStorage configStorage = wxMaProperties.getConfigStorage();
		WxMaRedisBetterConfigImpl wxMaRedisBetterConfig = new WxMaRedisBetterConfigImpl(wxRedisOps,  "wx_ma:" + maId);
		wxMaRedisBetterConfig.setAppid(wxMaProperties.getAppId());
		wxMaRedisBetterConfig.setSecret(wxMaProperties.getSecret());
		wxMaRedisBetterConfig.setToken(wxMaProperties.getToken());
		wxMaRedisBetterConfig.setAesKey(wxMaProperties.getAesKey());

		if (wxMaProperties.getHost() != null) {
			wxMaRedisBetterConfig.setApiHostUrl(wxMaProperties.getHost().getApiHost());
		}

		wxMaRedisBetterConfig.setHttpProxyHost(configStorage.getHttpProxyHost());
		wxMaRedisBetterConfig.setHttpProxyUsername(configStorage.getHttpProxyUsername());
		wxMaRedisBetterConfig.setHttpProxyPassword(configStorage.getHttpProxyPassword());

		if (configStorage.getHttpProxyPort() != null) {
			wxMaRedisBetterConfig.setHttpProxyPort(configStorage.getHttpProxyPort());
		}
		return wxMaRedisBetterConfig;
	}
}
