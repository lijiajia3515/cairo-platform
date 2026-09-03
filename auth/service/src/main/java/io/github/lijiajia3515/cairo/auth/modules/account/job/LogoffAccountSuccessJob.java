package io.github.lijiajia3515.cairo.auth.modules.account.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.message.account.DeletedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountSnsMongodb;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationCommonService;
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

@Slf4j
@Component
public class LogoffAccountSuccessJob {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;

	public LogoffAccountSuccessJob(MongoTemplate mongoTemplate,
								   TransactionTemplate transactionTemplate,
								   RabbitTemplate rabbitTemplate,
								   AccountAuthorizationCommonService accountAuthorizationCommonService,
								   CairoRabbitmqTool cairoRabbitmqTool,
								   ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
	}

	@XxlJob("logoffAccountSuccessJob")
	public void logoffAccountSuccessJob() {
		XxlJobHelper.log("job start");
		try {
			Criteria logoffedCriteria = Criteria
				.where(AccountMongodb.FIELD.LOGOFF_STATUS).is(AccountLogoffStatus.PENDING.getLogoffStatusValue())
				.and(AccountMongodb.FIELD.LOGOFF_PENDING_TIME).lte(LocalDateTime.now());
			Query logoffedQuery = Query.query(logoffedCriteria);

			Update accountUpdate = new Update();
			accountUpdate.set(AccountMongodb.FIELD.LOGOFF_STATUS, AccountLogoffStatus.SUCCESS.getLogoffStatusValue());
			accountUpdate.currentDate(AccountMongodb.FIELD.LOGOFF_SUCCESS_TIME);
			accountUpdate.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, null);
			accountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);

			Update accountPasswordUpdate = Update.update(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, null);
			accountPasswordUpdate.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);

			Update accountSnsUpdate = Update.update(AccountSnsMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, null);
			accountPasswordUpdate.currentDate(AccountSnsMongodb.FIELD.METADATA.UPDATE_TIME);

			List<AccountMongodb> logoffedAccountMongodbList = mongoTemplate.find(logoffedQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			logoffedAccountMongodbList.forEach(logoffedAccount -> {
				AccountMongodb deletedAccount = transactionTemplate.execute(status -> {
					try {
						// 删除账号
						Query deleteAccountQuery = Query.query(Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(logoffedAccount.getAccountId()));
						UpdateResult accountUpdateResult = mongoTemplate.updateFirst(deleteAccountQuery, accountUpdate, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
						log.debug("account update result: {}", accountUpdateResult);
						AccountMongodb deletedAccountMongodb = mongoTemplate.findAndRemove(deleteAccountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
						if (deletedAccountMongodb != null) {
							mongoTemplate.insert(deletedAccountMongodb, MongodbConstants.DeletedCollection.ACCOUNT);
						}

						// 删除账号密码表
						Query accountPasswordQuery = Query.query(Criteria.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(logoffedAccount.getAccountId()));
						UpdateResult accountPasswordUpdateResult = mongoTemplate.updateMulti(accountPasswordQuery, accountPasswordUpdate, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);
						log.debug("account password update result: {}", accountPasswordUpdateResult);
						List<AccountPasswordMongodb> removedAccountPasswordList = mongoTemplate.findAllAndRemove(accountPasswordQuery, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);
						if (!removedAccountPasswordList.isEmpty()) {
							mongoTemplate.insert(removedAccountPasswordList, MongodbConstants.DeletedCollection.ACCOUNT_PASSWORD);
						}

						// 删除账号第三方认证表
						Query accountSnsQuery = Query.query(Criteria.where(AccountSnsMongodb.FIELD.ACCOUNT_ID).is(logoffedAccount.getAccountId()));
						UpdateResult accountSnsUpdateResult = mongoTemplate.updateMulti(accountSnsQuery, accountSnsUpdate, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
						log.debug("account sns update result: {}", accountSnsUpdateResult);
						List<AccountSnsMongodb> removedAccountSnsList = mongoTemplate.findAllAndRemove(accountSnsQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
						if (!removedAccountSnsList.isEmpty()) {
							mongoTemplate.insert(removedAccountSnsList, MongodbConstants.DeletedCollection.ACCOUNT_SNS);
						}

						return deletedAccountMongodb;
					} catch (Exception e) {
						log.info("自动清理注销账号失败：", e);
						return null;
					}
				});

				try {
					if (deletedAccount != null) {
						// 发送账号删除消息
						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.DELETED_ACCOUNT),
							objectMapper.writeValueAsString(DeletedAccountMessage.builder()
								.accountId(deletedAccount.getAccountId())
								.nickname(deletedAccount.getNickname())
								.avatarUrl(deletedAccount.getAvatarUrl())
								.phoneNumber(deletedAccount.getPhoneNumber())
								.email(deletedAccount.getEmail())
								.username(deletedAccount.getUsername())
								.joinTime(deletedAccount.getJoinTime())
								.eventTime(LocalDateTime.now())
								.build()),
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
