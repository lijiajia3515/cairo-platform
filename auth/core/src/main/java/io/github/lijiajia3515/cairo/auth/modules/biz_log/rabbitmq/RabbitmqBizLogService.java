package io.github.lijiajia3515.cairo.auth.modules.biz_log.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.AccountBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.AppBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.SubappBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLogService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.ClientBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.OpenBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.TenantAppBizLog;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.TenantSubappBizLog;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;

import java.util.Optional;


public class RabbitmqBizLogService implements BizLogService {
	private final String appId;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;

	public RabbitmqBizLogService(String appId, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool, ObjectMapper objectMapper) {
		this.appId = appId;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
	}

	@Override
	public String getAppId() {
		return appId;
	}

	@Override
	@SneakyThrows
	@Async
	public void storeOpenBizLog(OpenBizLog bizLog) {
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(BizLogRabbitmqExchange.BIZ_LOG),
			cairoRabbitmqTool.getRouteKey().getAppKey(BizLogRabbitmqRouteKey.SEND_OPEN_BIZ_LOG, appId),
			objectMapper.writeValueAsString(bizLog),
			new CorrelationData(bizLog.getLogId())
		);
	}

	@Override
	@SneakyThrows
	@Async
	public void storeClientBizLog(ClientBizLog bizLog) {
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(BizLogRabbitmqExchange.BIZ_LOG),
			cairoRabbitmqTool.getRouteKey().getAppKey(BizLogRabbitmqRouteKey.SEND_CLIENT_BIZ_LOG, bizLog.getAppId()),
			objectMapper.writeValueAsString(bizLog),
			new CorrelationData(bizLog.getLogId())
		);
	}

	@Override
	@SneakyThrows
	@Async
	public void storeAccountBizLog(AccountBizLog bizLog) {
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(BizLogRabbitmqExchange.BIZ_LOG),
			cairoRabbitmqTool.getRouteKey().getAppKey(BizLogRabbitmqRouteKey.SEND_ACCOUNT_BIZ_LOG, Optional.ofNullable(bizLog.getAppId()).orElse(appId)),
			objectMapper.writeValueAsString(bizLog),
			new CorrelationData(bizLog.getLogId())
		);
	}

	@Override
	@SneakyThrows
	@Async
	public void storeAppBizLog(AppBizLog bizLog) {
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(BizLogRabbitmqExchange.BIZ_LOG),
			cairoRabbitmqTool.getRouteKey().getAppKey(BizLogRabbitmqRouteKey.SEND_APP_BIZ_LOG, bizLog.getAppId()),
			objectMapper.writeValueAsString(bizLog),
			new CorrelationData(bizLog.getLogId())
		);
	}

	@Override
	@SneakyThrows
	@Async
	public void storeSubappBizLog(SubappBizLog bizLog) {
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(BizLogRabbitmqExchange.BIZ_LOG),
			cairoRabbitmqTool.getRouteKey().getAppKey(BizLogRabbitmqRouteKey.SEND_SUBAPP_BIZ_LOG, bizLog.getAppId()),
			objectMapper.writeValueAsString(bizLog),
			new CorrelationData(bizLog.getLogId())
		);
	}

	@Override
	@SneakyThrows
	@Async
	public void storeTenantAppBizLog(TenantAppBizLog bizLog) {
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(BizLogRabbitmqExchange.BIZ_LOG),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(BizLogRabbitmqRouteKey.SEND_TENANT_APP_BIZ_LOG, bizLog.getTenantId(), bizLog.getAppId()),
			objectMapper.writeValueAsString(bizLog),
			new CorrelationData(bizLog.getLogId())
		);
	}

	@Override
	@SneakyThrows
	@Async
	public void storeTenantSubappBizLog(TenantSubappBizLog bizLog) {
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(BizLogRabbitmqExchange.BIZ_LOG),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(BizLogRabbitmqRouteKey.SEND_TENANT_SUBAPP_BIZ_LOG, bizLog.getTenantId(), bizLog.getAppId()),
			objectMapper.writeValueAsString(bizLog),
			new CorrelationData(bizLog.getLogId())
		);
	}

}
