package io.github.lijiajia3515.cairo.auth.modules.link;

import io.github.lijiajia3515.cairo.auth.domain.api.client.link.CreateBatchLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByLinkIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByShortUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.LinkInfo;

import java.util.List;

public interface LinkClientApiService {

	/**
	 * 批量创建短链地址
	 * 需要权限：link:create_link
	 * @param args 参数
	 * @return 短链信息集合
	 */
	List<LinkInfo> createBatchLink(CreateBatchLinkArgs args);

	/**
	 * 获取短链集合根据短链数组
	 * 需要权限：link:read
	 * @param args 参数
	 * @return 短链信息集合
	 */
	List<LinkInfo> getLinkListByShortUrl(GetLinkListByShortUrlArgs args);

	/**
	 * 获取短链集合根据短链数组
	 * 需要权限：link:read
	 * @param args 参数
	 * @return 短链信息集合
	 */
	List<LinkInfo> getLinkListByLinkId(GetLinkListByLinkIdArgs args);

}
