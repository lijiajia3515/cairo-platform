package io.github.lijiajia3515.cairo.auth.modules.imgproxy;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.imgproxy.GetImgUrlArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ImgproxyClientApiServiceImpl implements ImgproxyClientApiService {
	private final ImgproxyClientApiFeignClient imgproxyClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public ImgproxyClientApiServiceImpl(ImgproxyClientApiFeignClient imgproxyClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.imgproxyClientApiFeignClient = imgproxyClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<String> getProxyUrl(List<GetImgUrlArgs> params) {
		try {
			ResponseEntity<BusinessResult<List<String>>> proxyUrl = imgproxyClientApiFeignClient.getProxyUrl(cairoOAuthClientSdkService.getHeaderAuthorization(),params);
			return Optional.ofNullable(proxyUrl.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("deleteFile：", e);
			throw new ConflictBusinessException("获取图片代理url失败");
		}
	}
}
