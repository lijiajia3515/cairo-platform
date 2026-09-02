package io.github.lijiajia3515.cairo.auth.modules.biz_log.slf4j;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.AccountBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.AppBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.SubappBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLogService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.ClientBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.OpenBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.TenantAppBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.TenantSubappBizLog;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志记录服务 bizLog
 */
@Slf4j
public class Slf4jBizLogService implements BizLogService {
	private final String appId;

	public Slf4jBizLogService(String appId) {
		this.appId = appId;
	}

	@Override
	public String getAppId() {
		return appId;
	}

	@Override
	public void storeOpenBizLog(OpenBizLog bizLog) {
		log.info("open biz log: {}", bizLog);
	}

	@Override
	public void storeClientBizLog(ClientBizLog bizLog) {
		log.info("client biz log: {}", bizLog);
	}

	@Override
	public void storeAccountBizLog(AccountBizLog bizLog) {
		log.info("account biz log: {}", bizLog);
	}

	@Override
	public void storeAppBizLog(AppBizLog bizLog) {
		log.info("app biz log: {}", bizLog);
	}

	@Override
	public void storeSubappBizLog(SubappBizLog bizLog) {
		log.info("app subapp biz log: {}", bizLog);
	}

	@Override
	public void storeTenantAppBizLog(TenantAppBizLog bizLog) {
		log.info("tenant app biz log: {}", bizLog);
	}

	@Override
	public void storeTenantSubappBizLog(TenantSubappBizLog bizLog) {
		log.info("tenant app subapp biz log: {}", bizLog);
	}
}
