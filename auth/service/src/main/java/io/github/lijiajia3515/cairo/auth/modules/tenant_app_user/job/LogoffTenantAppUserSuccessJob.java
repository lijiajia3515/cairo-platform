package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.LogoffSuccessTenantAppUserMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自动注销企业应用级用户任务
 */
@Slf4j
@Component
public class LogoffTenantAppUserSuccessJob {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;

	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	private final ObjectMapper objectMapper;

	public LogoffTenantAppUserSuccessJob(MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
	}

	@XxlJob("logoffTenantAppUserSuccessJob")
	public void logoffTenantAppUserSuccessJob() {
		XxlJobHelper.log("job start");
		try {
			Criteria logoffSuccessCriteria = Criteria
				.where(TenantAppUserMongodb.FIELD.LOGOFF_STATUS).is(TenantAppUserLogoffStatus.PENDING.getLogoffStatusValue())
				.and(TenantAppUserMongodb.FIELD.LOGOFF_PENDING_TIME).lte(LocalDateTime.now());
			Query logoffedQuery = Query.query(logoffSuccessCriteria);


			List<TenantAppUserMongodb> logoffedUserMongodbList = mongoTemplate.find(logoffedQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
			logoffedUserMongodbList.forEach(logoffUserMongodb -> {
				TenantAppUserMongodb logoffSuccessUser = transactionTemplate.execute(status -> {
					try {
						// 注销用户关联账号
						Query logoffSuccessUserQuery = Query.query(Criteria
							.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(logoffUserMongodb.getTenantId())
							.and(TenantAppUserMongodb.FIELD.APP_ID).is(logoffUserMongodb.getAppId())
							.and(TenantAppUserMongodb.FIELD.USER_ID).is(logoffUserMongodb.getUserId())
						);

						Update userUpdate = new Update();
						userUpdate.set(TenantAppUserMongodb.FIELD.ACCOUNT_ID, null);
						userUpdate.set(TenantAppUserMongodb.FIELD.LOGOFF_STATUS, TenantAppUserLogoffStatus.SUCCESS.getLogoffStatusValue());
						userUpdate.set(TenantAppUserMongodb.FIELD.ADMIN, false);// 注销成功取消管理员身份
						userUpdate.currentDate(TenantAppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME);
						userUpdate.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, logoffUserMongodb.getUserId());
						userUpdate.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);

						TenantAppUserMongodb modifiedUserMongodb = mongoTemplate.findAndModify(logoffSuccessUserQuery, userUpdate, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
						// 将老账号ID，返回给外部发送消息到队列中
						if (modifiedUserMongodb != null) {
							modifiedUserMongodb.setAccountId(logoffUserMongodb.getAccountId());
						}
						return modifiedUserMongodb;
					} catch (Exception e) {
						log.info("自动清理注销用户失败：", e);
						return null;
					}
				});

				try {
					if (logoffSuccessUser != null) {
						// 发送账号删除消息
						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.LOGOFF_SUCCESS_TENANT_APP_USER, logoffSuccessUser.getTenantId(), logoffSuccessUser.getAppId()),
							objectMapper.writeValueAsString(
								LogoffSuccessTenantAppUserMessage.builder()
									.tenantId(logoffSuccessUser.getTenantId())
									.appId(logoffSuccessUser.getAppId())
									.userId(logoffSuccessUser.getUserId())
									.nickname(logoffSuccessUser.getNickname())
									.accountId(logoffSuccessUser.getAccountId())
									.eventUserId(logoffSuccessUser.getUserId())
									.eventTime(LocalDateTime.now())
									.build()
							),
							new CorrelationData(CoreConstants.nextIdStr())
						);
					}
				} catch (Exception e) {
					log.info("e", e);
				}

			});

		} catch (Exception e) {
			log.error("error{}", e.getMessage());
			XxlJobHelper.log(e.getMessage());
			XxlJobHelper.handleFail("job fail");
			throw new RuntimeException(e);
		}
		XxlJobHelper.log("job end");
	}
}
