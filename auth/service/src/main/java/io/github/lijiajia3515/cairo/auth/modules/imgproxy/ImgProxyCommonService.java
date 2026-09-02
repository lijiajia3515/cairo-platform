package io.github.lijiajia3515.cairo.auth.modules.imgproxy;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.framework.imgproxy.ImgProxy;
import io.github.lijiajia3515.cairo.auth.framework.imgproxy.ImgProxyProperties;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.stereotype.Service;

import java.util.Map;
/**
 * [common] imgproxy service
 */
@Service
public class ImgProxyCommonService {
	final byte[] KEY;
	final byte[] SLAT;

	private final ImgProxyProperties properties;

	public ImgProxyCommonService(ImgProxyProperties properties) {
		this.properties = properties;
		KEY = ImgProxy.hexStringToByteArray(properties.getKey());
		SLAT = ImgProxy.hexStringToByteArray(properties.getSlat());
	}

	/**
	 * @param url    url
	 * @param params 参数
	 * @return URL地址
	 */
	@NewSpan
	@BizLog(
		bizId = "imgproxy:get_proxy_url",
		scope = "write",
		params = {
			@BizLog.Param(key = "url", value = "#url"),
			@BizLog.Param(key = "params", value = "#params"),
		}
	)
	public String getProxyUrl(String url, Map<String, String> params) {
		return properties.getServer() + ImgProxy.generateSignedUrl(KEY, SLAT, url, params);
	}
}
