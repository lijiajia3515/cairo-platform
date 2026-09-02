package io.github.lijiajia3515.cairo.auth.modules.subapp_version;

import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_version.GetSubappVersionClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.SubappVersion;

import java.util.List;


public interface SubappVersionClientApiService {

	/**
	 * 获取子应用版本列表
	 *
	 * @param args 参数
	 * @return 子应用版本 列表模式
	 */
	List<SubappVersion> getSubappVersionList(GetSubappVersionClientArgs args);

}
