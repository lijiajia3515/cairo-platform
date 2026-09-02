package io.github.lijiajia3515.cairo.auth.modules.app;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.core.page.Page;

import java.util.List;


public interface AppClientApiService {
	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	List<App> getAppList(GetAppArgs args);

	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	Page<App> getAppPageList(GetAppArgs args);
}
