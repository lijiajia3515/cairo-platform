package io.github.lijiajia3515.cairo.auth.modules.subapp;


import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;

import java.util.List;


public interface SubappClientApiService {

	/**
	 * 获取子应用列表
	 *
	 * @param args 参数
	 * @return 子应用 列表模式
	 */
	List<Subapp> getSubappList(GetSubappClientArgs args);

}
