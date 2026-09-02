package io.github.lijiajia3515.cairo.auth.modules.link;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.framework.sdk.sign.SignResp;
import io.github.lijiajia3515.cairo.auth.framework.sdk.sign.SignSdkTools;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.CreateBatchLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByLinkIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByShortUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.LinkInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class LinkClientApiServiceImpl implements LinkClientApiService{

	private final LinkClientApiFeignClient linkClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public LinkClientApiServiceImpl(LinkClientApiFeignClient linkClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.linkClientApiFeignClient = linkClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<LinkInfo> createBatchLink(CreateBatchLinkArgs args) {
		try {
			SignResp sign = SignSdkTools.sign();
			ResponseEntity<BusinessResult<List<LinkInfo>>> batchLink = linkClientApiFeignClient.createBatchLink(cairoOAuthClientSdkService.getHeaderAuthorization(),sign.getTimestamp(),sign.getNonce(),sign.getSign(),args);
			return Optional.ofNullable(batchLink.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("createBatchLink：", e);
			throw new ConflictBusinessException("批量创建短链地址失败");
		}
	}

	@Override
	public List<LinkInfo> getLinkListByShortUrl(GetLinkListByShortUrlArgs args) {
		try {
			ResponseEntity<BusinessResult<List<LinkInfo>>> linkListByShortUrl = linkClientApiFeignClient.getLinkListByShortUrl(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(linkListByShortUrl.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getLinkListByShortUrl：", e);
			throw new ConflictBusinessException("获取短链集合根据短链数组失败");
		}
	}

	@Override
	public List<LinkInfo> getLinkListByLinkId(GetLinkListByLinkIdArgs args) {
		try {
			ResponseEntity<BusinessResult<List<LinkInfo>>> linkListByLinkId = linkClientApiFeignClient.getLinkListByLinkId(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(linkListByLinkId.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getLinkListByShortUrl：", e);
			throw new ConflictBusinessException("获取短链集合根据短链数组失败");
		}
	}
}
