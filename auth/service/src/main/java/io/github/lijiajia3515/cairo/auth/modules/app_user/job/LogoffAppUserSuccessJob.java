package io.github.lijiajia3515.cairo.auth.modules.app_user.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.LogoffSuccessAppUserMessage;
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
 * 注销应用用户任务
 */
@Slf4j
@Component
public class LogoffAppUserSuccessJob {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;

	public LogoffAppUserSuccessJob(MongoTemplate mongoTemplate,
								   TransactionTemplate transactionTemplate,
								   RabbitTemplate rabbitTemplate,
								   CairoRabbitmqTool cairoRabbitmqTool,
								   ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
	}

	@XxlJob("logoffAppUserSuccessJob")
	public void logoffAppUerSuccessJob() throws Exception {
		XxlJobHelper.log("job start");
		try {
			Criteria logoffSuccessCriteria = Criteria
				.where(AppUserMongodb.FIELD.ACCOUNT_ID).ne(null)
				.and(AppUserMongodb.FIELD.LOGOFF_STATUS).is(AppUserLogoffStatus.PENDING.getLogoffStatusValue())
				.and(AppUserMongodb.FIELD.LOGOFF_PENDING_TIME).lte(LocalDateTime.now());
			Query logoffedQuery = Query.query(logoffSuccessCriteria);

			List<AppUserMongodb> logoffedAppUserMongodbList = mongoTemplate.find(logoffedQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
			logoffedAppUserMongodbList.forEach(logoffUserMongodb -> {
				AppUserMongodb logoffSuccessAppUser = transactionTemplate.execute(status -> {
					try {
						// 注销应用用户关联账号
						Query logoffSuccessUserQuery = Query.query(Criteria
							.where(AppUserMongodb.FIELD.APP_ID).is(logoffUserMongodb.getAppId())
							.and(AppUserMongodb.FIELD.USER_ID).is(logoffUserMongodb.getUserId())
						);

						Update userUpdate = new Update();
						userUpdate.set(AppUserMongodb.FIELD.ACCOUNT_ID, null);
						userUpdate.set(AppUserMongodb.FIELD.LOGOFF_STATUS, AppUserLogoffStatus.SUCCESS.getLogoffStatusValue());
						userUpdate.currentDate(AppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME);
						userUpdate.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, logoffUserMongodb.getUserId());
						userUpdate.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);

						AppUserMongodb modifiedUserMongodb = mongoTemplate.findAndModify(logoffSuccessUserQuery, userUpdate, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
						// 将老账号ID，返回给外部发送消息到队列中
						if (modifiedUserMongodb != null) {
							modifiedUserMongodb.setAccountId(logoffUserMongodb.getAccountId());
						}
						return modifiedUserMongodb;
					} catch (Exception e) {
						log.info("注销应用用户失败：", e);
						return null;
					}
				});

				try {
					if (logoffSuccessAppUser != null) {
						// 发送注销用户消息
						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.LOGOFF_SUCCESS_APP_USER, logoffSuccessAppUser.getAppId()),
							objectMapper.writeValueAsString(
								LogoffSuccessAppUserMessage.builder()
									.appId(logoffSuccessAppUser.getAppId())
									.userId(logoffSuccessAppUser.getUserId())
									.nickname(logoffSuccessAppUser.getNickname())
									.accountId(logoffSuccessAppUser.getAccountId())
									.eventAppUserId(logoffSuccessAppUser.getUserId())
									.eventTime(LocalDateTime.now())
									.build()
							),
							new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
						);
					}
				} catch (Exception e) {
					log.info("e", e);
				}

			});

		} catch (Exception e) {
			log.error("error {}", e.getMessage());
			XxlJobHelper.log(e.getMessage());
			XxlJobHelper.handleFail("job fail");
			throw new RuntimeException(e);
		}
		XxlJobHelper.log("job end");
	}
}
