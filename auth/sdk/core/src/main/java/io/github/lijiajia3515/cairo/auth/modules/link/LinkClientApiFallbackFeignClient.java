package io.github.lijiajia3515.cairo.auth.modules.link;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.CreateBatchLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByLinkIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByShortUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.LinkInfo;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api link fallback feign client
 */
public class LinkClientApiFallbackFeignClient implements LinkClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-短链子应用故障");

	@Override
	public ResponseEntity<BusinessResult<List<LinkInfo>>> createBatchLink(String authorization,String timestamp,String nonce,String sign,CreateBatchLinkArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<LinkInfo>>> getLinkListByShortUrl(String authorization,GetLinkListByShortUrlArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<LinkInfo>>> getLinkListByLinkId(String authorization,GetLinkListByLinkIdArgs args) {
		throw EX;
	}
}
